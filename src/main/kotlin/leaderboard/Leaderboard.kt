package com.example.leaderboard

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.ktor.http.*
import java.lang.StringBuilder
import com.example.apiToken
import com.example.client
import com.example.json
import com.example.shopUrl
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

data class Customer(val id: String, val amountSpent: Double)

suspend fun fetchLeaderboardCsv(): String {
    val desiredCount = 50
    val maxIterations = 20
    val epsilon = 100.0
    var low = 0.0
    var high = 1_000_000.00  // Assumed upper limit of spending
    var threshold = 0.0
    var lastGoodThreshold = 0.0

    for (i in 1..maxIterations) {
        val midRaw = (low + high) / 2
        val mid = "%.2f".format(midRaw).toDouble()

        val (count, hasNextPage) = queryCustomersCountAndPageInfo(mid, desiredCount)
        println("Binary search iteration $i: threshold=$mid count=$count hasNextPage=$hasNextPage")

        if (count >= desiredCount || hasNextPage) {
            low = mid
            lastGoodThreshold = mid
        } else {
            high = mid
        }

        if ((high - low) < epsilon) {
            println("Converged early at threshold=$lastGoodThreshold")
            break  // ✅ Valid here — we're inside a real loop
        }
    }

    threshold = lastGoodThreshold
    println("Final threshold for >= $desiredCount customers: $threshold")

    // Step 2: Fetch all customers above threshold
    val allCustomers = mutableListOf<Customer>()
    var cursor: String? = null

    do {
        val (customers, pageInfo) = fetchCustomersAboveThreshold(threshold, 250, cursor)
        allCustomers.addAll(customers)
        cursor = pageInfo.endCursor
    } while (pageInfo.hasNextPage)

    // Step 3: Sort and select top N customers
    println("Fetched ${allCustomers.size} customers above threshold")
    val topCustomers = allCustomers.sortedByDescending { it.amountSpent }.take(desiredCount)

    // Step 4: Build CSV
    val csvBuilder = StringBuilder()
    csvBuilder.append("Customer ID,Amount Spent\n")
    for (customer in topCustomers) {
        csvBuilder.append("${customer.id},${"%.2f".format(customer.amountSpent)}\n")
    }

    return csvBuilder.toString()
}


/**
 * Queries customers count and page info for customers with total_spent > threshold.
 * Uses 'first' = pageSize to detect if count exceeds desiredCount.
 * Returns actual count in the page (<= pageSize) and if there is a next page.
 */
suspend fun queryCustomersCountAndPageInfo(threshold: Double, pageSize: Int): Pair<Int, Boolean> {
    val query = """
        query customersAbove(${"$"}first: Int!) {
          customers(
            first: ${"$"}first
            query: "total_spent:>${"%.2f".format(threshold)}"
          ) {
            edges {
              cursor
              node { id }
            }
            pageInfo {
              hasNextPage
              endCursor
            }
          }
        }
    """

    val variables = buildJsonObject {
        put("first", pageSize) // pageSize = desiredCount = 50
    }

    val response = client.post(shopUrl) {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            append("X-Shopify-Access-Token", apiToken)
        }
        setBody(buildJsonObject {
            put("query", query)
            put("variables", variables)
        }.toString())
    }

    val text = response.bodyAsText()
    val graphQLResponse = json.decodeFromString<GraphQLResponse<CustomersData>>(text)
    val customersConnection = graphQLResponse.data?.customers

    val count = customersConnection?.edges?.size ?: 0
    val hasNextPage = customersConnection?.pageInfo?.hasNextPage ?: false

    return Pair(count, hasNextPage)
}

suspend fun fetchCustomersAboveThreshold(
    threshold: Double,
    first: Int,
    afterCursor: String?
): Pair<List<Customer>, PageInfo> {
    val query = """
        query customersAbove(${"$"}first: Int!, ${"$"}after: String) {
          customers(
            first: ${"$"}first
            after: ${"$"}after
            query: "total_spent:>${"%.2f".format(threshold)}"
          ) {
            edges {
              node {
                id
                amountSpent {
                  amount
                  currencyCode
                }
              }
            }
            pageInfo {
              hasNextPage
              endCursor
            }
          }
        }
    """

    val variables = buildJsonObject {
        put("first", first)
        if (afterCursor != null) put("after", afterCursor)
    }

    val response = client.post(shopUrl) {
        headers {
            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            append(HttpHeaders.Accept, ContentType.Application.Json.toString())
            append("X-Shopify-Access-Token", apiToken)
        }
        setBody(buildJsonObject {
            put("query", query)
            put("variables", variables)
        }.toString())
    }

    val text = response.bodyAsText()
    val graphQLResponse = json.decodeFromString<GraphQLResponse<CustomersData>>(text)
    val customersConnection = graphQLResponse.data?.customers

    val customers = customersConnection?.edges
        ?.mapNotNull { it.node }
        ?.map {
            val amount = it.amountSpent?.amount?.toDoubleOrNull() ?: 0.0
            Customer(it.id, amount)
        } ?: emptyList()

    val pageInfo = customersConnection?.pageInfo ?: PageInfo(false, null)

    return Pair(customers, pageInfo)
}

// Data classes for deserialization

@Serializable
data class GraphQLResponse<T>(
    val data: T?
)

@Serializable
data class CustomersData(
    val customers: CustomersConnection?
)

@Serializable
data class CustomersConnection(
    val edges: List<CustomerEdge>?,
    val pageInfo: PageInfo
)

@Serializable
data class CustomerEdge(
    val cursor: String? = null,
    val node: CustomerNode?
)

@Serializable
data class Money(
    val amount: String,
    val currencyCode: String
)

@Serializable
data class CustomerNode(
    val id: String,
    val amountSpent: Money? = null
)

@Serializable
data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String?
)
