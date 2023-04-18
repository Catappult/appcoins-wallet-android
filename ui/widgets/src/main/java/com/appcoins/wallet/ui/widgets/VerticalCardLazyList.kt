package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime

class VerticalCardLazyList {

    @Composable
    fun VerticalCardList(
        contents: List<Any>,
        onCardClick: () -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(contents) { content ->
                CardItem(item = content, onCardClick = onCardClick())
            }
        }
    }


    //This is sample to use this HorizontalCardList

    @Composable
    fun CardItem(item: Any, onCardClick: Unit) {
        if (item is CardItemTest) {
            Card(
                backgroundColor = MaterialTheme.colors.background,
                elevation = 2.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.clickable { onCardClick }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = item.title, style = MaterialTheme.typography.h5)
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Promotion starts on ${item.promotionTime}",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            // Handle case where item is not a Card
            Text(text = "Unsupported item type")
        }
    }

    @Preview
    @Composable
    fun VerticalCardListPreview() {
        val items = listOf(
            CardItemTest(
                title = "Card 1",
                subtitle = "Subtitle 1",
                promotionTime = LocalDateTime.of(2023, 4, 4, 10, 20),
                action = { },
                imageUrl = "https://example.com",
                urlRedirect = "https://example.com"
            ),
            CardItemTest(
                title = "Card 2",
                subtitle = "Subtitle 2",
                promotionTime = LocalDateTime.of(2023, 3, 1, 0, 30),
                action = { },
                imageUrl = "https://example.com",
                urlRedirect = "https://example.com"
            ),
            CardItemTest(
                title = "Card 3",
                subtitle = "Subtitle 3",
                promotionTime = LocalDateTime.of(2023, 3, 2, 10, 0),
                action = { },
                imageUrl = "https://example.com",
                urlRedirect = "https://example.com"
            )
        )
        VerticalCardList(contents = items, onCardClick = {})
    }
}

