package com.kimothorick.soloshelf.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun testNavBackStack(stack: NavBackStack<NavKey>) {
    stack.add(Route.Library)
    stack.removeLastOrNull()
}
