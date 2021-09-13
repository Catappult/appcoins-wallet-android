package cm.aptoide.skills.repository

sealed class StoredTicket

object EmptyStoredTicket : StoredTicket()

class StoredTicketInQueue(val ticketId: String) : StoredTicket()