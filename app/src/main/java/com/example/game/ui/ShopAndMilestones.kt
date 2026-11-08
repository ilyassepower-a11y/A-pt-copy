package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.data.GameSaveData

// ---------------------------------------------------------------------------
// Small bar on the main menu: balance, shop and milestones
// ---------------------------------------------------------------------------
@Composable
fun MenuTopBar(
    shop: ShopState,
    hasClaimable: Boolean,
    onShop: () -> Unit,
    onMilestones: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Pill(text = "🍕 ${shop.balance}", onClick = null)
        Pill(text = "🛒", onClick = onShop)
        Box {
            Pill(text = "🏆", onClick = onMilestones)
            if (hasClaimable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .background(Color(0xFFFF1744), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun Pill(text: String, onClick: (() -> Unit)?) {
    val base = Modifier
        .height(38.dp)
        .clip(RoundedCornerShape(19.dp))
        .background(Color(0xCC1E1530))
    val clickableModifier = if (onClick != null) base.clickable { onClick() } else base
    Box(
        modifier = clickableModifier.padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// Shop
// ---------------------------------------------------------------------------
@Composable
fun ShopDialog(
    shop: ShopState,
    onBuy: (PowerUp) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF1E1530)) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛒 المتجر", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("🍕 ${shop.balance}", color = Color(0xFFFFD54F), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "اشتري قدرات واستعملها وسط اللعب بالأزرار اللي على جنب الشاشة. المكونات اللي كتجمع هي العملة.",
                    color = Color(0xFFBDB6CC),
                    fontSize = 13.sp
                )
                for (item in PowerUp.values()) {
                    ShopRow(item = item, shop = shop, onBuy = onBuy)
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق")
                }
            }
        }
    }
}

@Composable
private fun ShopRow(item: PowerUp, shop: ShopState, onBuy: (PowerUp) -> Unit) {
    val canBuy = shop.balance >= item.cost
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A1F40))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(item.color.copy(alpha = 0.85f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.icon, fontSize = 24.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.titleAr, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(item.descAr, color = Color(0xFFBDB6CC), fontSize = 12.sp)
            Text("عندك: ${shop.count(item)}", color = Color(0xFFFFD54F), fontSize = 12.sp)
        }
        Button(
            onClick = { onBuy(item) },
            enabled = canBuy,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
        ) {
            Text("${item.cost} 🍕", fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// Milestones
// ---------------------------------------------------------------------------
@Composable
fun MilestonesDialog(
    save: GameSaveData,
    shop: ShopState,
    onClaim: (Milestone) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF1E1530)) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆 الإنجازات", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("🍕 ${shop.balance}", color = Color(0xFFFFD54F), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "حقق الأهداف واستلم مكونات كمكافأة تقدر تصرفها فالمتجر.",
                    color = Color(0xFFBDB6CC),
                    fontSize = 13.sp
                )
                for (milestone in ALL_MILESTONES) {
                    MilestoneRow(milestone = milestone, save = save, shop = shop, onClaim = onClaim)
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق")
                }
            }
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: Milestone,
    save: GameSaveData,
    shop: ShopState,
    onClaim: (Milestone) -> Unit
) {
    val progress = milestone.progressOf(save)
    val done = progress >= milestone.target
    val claimed = milestone.id in shop.claimed
    val fraction = (progress.toFloat() / milestone.target.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A1F40))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(milestone.icon, fontSize = 28.sp, modifier = Modifier.alpha(if (done) 1f else 0.5f))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(milestone.titleAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(milestone.descAr, color = Color(0xFFBDB6CC), fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF3A2D52))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(if (done) Color(0xFF66BB6A) else Color(0xFFFFB300))
                )
            }
            Text(
                "${minOf(progress, milestone.target)} / ${milestone.target}",
                color = Color(0xFFBDB6CC),
                fontSize = 11.sp
            )
        }
        when {
            claimed -> Text("✅", fontSize = 22.sp)
            done -> Button(
                onClick = { onClaim(milestone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Text("+${milestone.reward} 🍕", fontWeight = FontWeight.Bold)
            }
            else -> Text("+${milestone.reward} 🍕", color = Color(0xFF8E86A0), fontSize = 13.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// In-game item buttons (right side of the screen)
// ---------------------------------------------------------------------------
@Composable
fun PowerUpBar(
    shop: ShopState,
    enabled: Boolean,
    onUse: (PowerUp) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (item in PowerUp.values()) {
            val count = shop.count(item)
            val active = enabled && count > 0
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .alpha(if (active) 1f else 0.45f)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.9f))
                    .clickable(enabled = active) { onUse(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(item.icon, fontSize = 24.sp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(Color(0xFF000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$count", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
