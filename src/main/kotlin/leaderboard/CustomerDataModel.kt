//package com.example.leaderboard
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class Money(
//    val amount: String,
//    val currencyCode: String
//)
//
//@Serializable
//data class CustomerNode(
//    val id: String,
//    val amountSpent: Money? = null // matches Shopify's amountSpent field
//)
//
//@Serializable
//data class CustomersEdge(
//    val cursor: String,
//    val node: CustomerNode? = null
//)
//
//@Serializable
//data class PageInfo(
//    val hasNextPage: Boolean,
//    val endCursor: String? = null
//)
//
//@Serializable
//data class CustomersConnection(
//    val edges: List<CustomersEdge>,
//    val pageInfo: PageInfo
//)
//
//@Serializable
//data class CustomersData(
//    val customers: CustomersConnection
//)
//
//@Serializable
//data class GraphQLExtensionsCost(
//    val requestedQueryCost: Int
//)
//
//@Serializable
//data class GraphQLExtensions(
//    val cost: GraphQLExtensionsCost
//)
//
//@Serializable
//data class GraphQLError(
//    val message: String,
//    val locations: List<Location>? = null,
//    val path: List<String>? = null
//)
//
//@Serializable
//data class Location(
//    val line: Int,
//    val column: Int
//)
//
//@Serializable
//data class GraphQLResponse<T>(
//    val data: T? = null,
//    val errors: List<GraphQLError>? = null,
//    val extensions: GraphQLExtensions? = null
//)
//
//// For internal processing or CSV export
//@Serializable
//data class Customer(
//    val id: String,
//    val amountSpent: Double
//)
