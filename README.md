# 🏆 Shopify Leaderboard Exporter

This repository implements a backend solution to generate a **CSV leaderboard** of the top 50 highest-spending customers with specific Shopify tags using **GraphQL API**. It is tailored for a Shopify backend interview task under performance and API constraints.

---

## 🚀 Features

- Fetches **customers tagged with `task1` and `level:3`**
- Calculates top **50 customers by `amountSpent`**
- Optimizes data fetching using **binary search and pagination**
- Outputs a clean, two-column CSV: `Customer ID, Amount Spent`
- Built with **Kotlin + Ktor client** and **Shopify GraphQL 2025-01 API**

---

## 📄 Example Output (CSV)
<img width="691" alt="Screenshot 2025-05-23 at 3 07 54 PM" src="https://github.com/user-attachments/assets/f6fbe073-6b40-43ed-a5f4-dbb8efa9a1f3" />


## 🧠 Task Summary

This project solves the following Shopify backend interview task:

**Export the top 50 customers** tagged with `task1` and `level:3`, ranked by `amountSpent`, using **GraphQL API** only.  
> Must complete within a **5-second timeout** and handle **rate limits efficiently**.

### Approach Explanation
The goal is to efficiently retrieve the top 50 highest-spending customers tagged with task1 and level:3, using Shopify's GraphQL API (2025-01 version) under time and rate limits.

Given the customer count (>100,000), directly querying all and sorting them isn't viable. So the approach uses a threshold-based binary search to reduce the search space.

✅ Step-by-step Breakdown
1. Binary Search for Minimum Threshold
Why? To avoid fetching all 100k+ customers, I binary search a spending threshold above which at least 50 customers exist.
How? I start with a range (0.0 to 1,000,000.0) and iteratively narrow it using:

```kotlin
query: "total_spent:>${threshold} AND tag:task1 AND tag:level:3"
```
- I fetch a page of customers (50) and check:
- If I get 50 customers and if there's a hasNextPage = true, it means more customers exist above that threshold.
- Else, reduce the threshold.
- Continue until the difference between high and low is less than a small epsilon (e.g., 100.0) or until max iterations.

## ⚙️ Setup Instructions

**Clone the repo**:
   ```bash
   git clone https://github.com/nidbhardwaj/Leaderboard.git
   cd Leaderboard


   





