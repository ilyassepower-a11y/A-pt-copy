package com.example.game.ui

import android.content.Context
import android.content.SharedPreferences

// What the player owns: spendable toppings, bought items and claimed milestones
data class ShopState(
    val balance: Int = 0,
    val inventory: Map<String, Int> = emptyMap(),
    val claimed: Set<String> = emptySet(),
    val ownedSkins: Set<String> = setOf("chef"),
    val selectedSkin: String = "chef"
) {
    fun count(item: PowerUp): Int = inventory[item.id] ?: 0
    fun owns(skin: Skin): Boolean = skin.id in ownedSkins
}

class ShopRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pizza_escape_prefs", Context.MODE_PRIVATE)

    init {
        // Players who already collected toppings start with them as their balance
        if (!prefs.contains("shop_balance")) {
            prefs.edit().putInt("shop_balance", prefs.getInt("total_toppings", 0)).apply()
        }
    }

    fun load(): ShopState {
        val inventory = HashMap<String, Int>()
        for (item in PowerUp.values()) {
            inventory[item.id] = prefs.getInt("inv_" + item.id, 0)
        }
        val claimed = HashSet<String>(prefs.getStringSet("claimed_milestones", emptySet()) ?: emptySet())
        val owned = HashSet<String>(prefs.getStringSet("owned_skins", emptySet()) ?: emptySet())
        owned.add("chef")
        var selected = prefs.getString("selected_skin", "chef") ?: "chef"
        if (selected !in owned) selected = "chef"
        return ShopState(prefs.getInt("shop_balance", 0), inventory, claimed, owned, selected)
    }

    fun addToppings(amount: Int) {
        if (amount <= 0) return
        prefs.edit().putInt("shop_balance", prefs.getInt("shop_balance", 0) + amount).apply()
    }

    fun buy(item: PowerUp): Boolean {
        val balance = prefs.getInt("shop_balance", 0)
        if (balance < item.cost) return false
        prefs.edit()
            .putInt("shop_balance", balance - item.cost)
            .putInt("inv_" + item.id, prefs.getInt("inv_" + item.id, 0) + 1)
            .apply()
        return true
    }

    fun consume(item: PowerUp): Boolean {
        val owned = prefs.getInt("inv_" + item.id, 0)
        if (owned <= 0) return false
        prefs.edit().putInt("inv_" + item.id, owned - 1).apply()
        return true
    }

    fun claim(milestone: Milestone): Boolean {
        val claimed = HashSet<String>(prefs.getStringSet("claimed_milestones", emptySet()) ?: emptySet())
        if (milestone.id in claimed) return false
        claimed.add(milestone.id)
        prefs.edit()
            .putStringSet("claimed_milestones", claimed)
            .putInt("shop_balance", prefs.getInt("shop_balance", 0) + milestone.reward)
            .apply()
        return true
    }

    fun buySkin(skin: Skin): Boolean {
        if (skin.adminOnly || skin.price <= 0) return false
        val owned = HashSet<String>(prefs.getStringSet("owned_skins", emptySet()) ?: emptySet())
        if (skin.id in owned) return false
        val balance = prefs.getInt("shop_balance", 0)
        if (balance < skin.price) return false
        owned.add(skin.id)
        prefs.edit()
            .putInt("shop_balance", balance - skin.price)
            .putStringSet("owned_skins", owned)
            .putString("selected_skin", skin.id)
            .apply()
        return true
    }

    fun unlockSkin(skin: Skin): Boolean {
        val owned = HashSet<String>(prefs.getStringSet("owned_skins", emptySet()) ?: emptySet())
        if (skin.id in owned) return false
        owned.add(skin.id)
        prefs.edit()
            .putStringSet("owned_skins", owned)
            .putString("selected_skin", skin.id)
            .apply()
        return true
    }

    fun selectSkin(skin: Skin) {
        val owned = HashSet<String>(prefs.getStringSet("owned_skins", emptySet()) ?: emptySet())
        owned.add("chef")
        if (skin.id in owned) {
            prefs.edit().putString("selected_skin", skin.id).apply()
        }
    }
}
