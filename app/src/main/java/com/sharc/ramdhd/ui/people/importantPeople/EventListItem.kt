package com.sharc.ramdhd.ui.people.importantPeople

import com.sharc.ramdhd.data.model.importantPeople.ImportantEvent

sealed class EventListItem {
    data class EventItem(val event: ImportantEvent) : EventListItem()
    data class HeaderItem(val title: String) : EventListItem()
}