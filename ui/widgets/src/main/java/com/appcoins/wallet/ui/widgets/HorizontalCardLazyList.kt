package com.appcoins.wallet.ui.widgets


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime

class HorizontalCardLazyList {
    @Composable
    fun HorizontalCardList(
        items: List<Card>,
        onCardClick: () -> Unit
    ) {
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { card ->
                CardItem(card = card, onCardClick = onCardClick)
            }
        }
    }

    //This is sample to use this HorizontalCardList

    data class Card(
        val title: String,
        val subtitle: String,
        val promotionTime: LocalDateTime,
        val action: () -> Unit
    )

    @Composable
    fun CardItem(card: Card, onCardClick: () -> Unit) {
        Card(
          colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
          elevation = CardDefaults.cardElevation(2.dp),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
          Column(modifier = Modifier.clickable { onCardClick.invoke() }) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(text = card.title, style = MaterialTheme.typography.titleMedium)
              Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
              Text(
                text = "Promotion starts on ${card.promotionTime}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
              )
            }
            }
        }
    }

    @Preview
    @Composable
    fun HorizontalCardListPreview() {
        val items = listOf(
            Card(
                title = "Card 1",
                subtitle = "Subtitle 1",
                promotionTime = LocalDateTime.of(2022, 1, 1, 0, 0),
                action = { }
            ),
            Card(
                title = "Card 2",
                subtitle = "Subtitle 2",
                promotionTime = LocalDateTime.of(2022, 2, 1, 0, 0),
                action = { }
            ),
            Card(
                title = "Card 3",
                subtitle = "Subtitle 3",
                promotionTime = LocalDateTime.of(2024, 1, 1, 0, 0),
                action = { }
            )
        )
        HorizontalCardList(items = items, onCardClick = {})
    }
}
