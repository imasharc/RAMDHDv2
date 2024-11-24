package com.sharc.ramdhd.ui.people.importantPeople

enum class EventFilterType {
    ALL,
    BY_PERSON,
    BY_EVENT_TYPE,
    THIS_WEEK,
    THIS_MONTH;

    override fun toString(): String {
        return when (this) {
            ALL -> "All Events"
            BY_PERSON -> "By Person"
            BY_EVENT_TYPE -> "By Event Type"
            THIS_WEEK -> "This Week"
            THIS_MONTH -> "This Month"
        }
    }
}