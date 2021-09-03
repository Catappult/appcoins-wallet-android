package com.asfoundation.wallet.rating.network

data class WalletFeedbackBody(val ticket: Ticket) {
  data class Ticket(val subject: String, val comment: Comment)
  data class Comment(val body: String)
}