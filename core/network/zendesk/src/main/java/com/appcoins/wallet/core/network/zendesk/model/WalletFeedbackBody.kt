package com.appcoins.wallet.core.network.zendesk.model

data class WalletFeedbackBody(val ticket: Ticket) {
  data class Ticket(val subject: String, val comment: Comment)
  data class Comment(val body: String)
}