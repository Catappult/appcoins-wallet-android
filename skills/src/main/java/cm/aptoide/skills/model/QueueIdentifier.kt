package cm.aptoide.skills.model

data class QueueIdentifier(
  // when Ticket is CreatedTicket, we might not have a queueId yet
  val id: String?,
  val setByUser: Boolean
)
