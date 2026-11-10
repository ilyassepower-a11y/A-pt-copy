package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.data.GameSaveData

private val PanelColor = Color(0xFF1E1530)
private val CardColor = Color(0xFF2A1F40)
private val MutedText = Color(0xFFBDB6CC)
private val GoldText = Color(0xFFFFD54F)

// ---------------------------------------------------------------------------
// Small pieces
// ---------------------------------------------------------------------------
@Composable
private fun Coin(size: Int = 20) {
    Image(
        painter = painterResource(id = R.drawable.ic_coin),
        contentDescription = null,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
private fun BalanceChip(balance: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCC1E1530))
            .padding(start = 10.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Coin(24)
        Spacer(modifier = Modifier.width(6.dp))
        Text("$balance", color = GoldText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RoundIconButton(iconRes: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xCC1E1530))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun DialogTitle(iconRes: Int, title: String, balance: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        BalanceChip(balance)
    }
}

// ---------------------------------------------------------------------------
// Top bar of the main menu: balance, shop, skins and milestones
// ---------------------------------------------------------------------------
@Composable
fun MenuTopBar(
    shop: ShopState,
    hasClaimable: Boolean,
    onShop: () -> Unit,
    onSkins: () -> Unit,
    onMilestones: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BalanceChip(shop.balance)
        RoundIconButton(R.drawable.ic_shop, onShop)
        RoundIconButton(R.drawable.ic_skins, onSkins)
        Box {
            RoundIconButton(R.drawable.ic_trophy, onMilestones)
            if (hasClaimable) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .background(Color(0xFFFF1744), CircleShape)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Items shop
// ---------------------------------------------------------------------------
@Composable
fun ShopDialog(
    shop: ShopState,
    onBuy: (PowerUp) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = PanelColor) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogTitle(R.drawable.ic_shop, "المتجر", shop.balance)
                Text(
                    "اشتري قدرات واستعملها وسط اللعب بالأزرار اللي على جنب الشاشة. المكونات اللي كتجمع هي العملة.",
                    color = MutedText,
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
            .background(CardColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(item.color.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.titleAr, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(item.descAr, color = MutedText, fontSize = 12.sp)
            Text("عندك: ${shop.count(item)}", color = GoldText, fontSize = 12.sp)
        }
        Button(
            onClick = { onBuy(item) },
            enabled = canBuy,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
        ) {
            Text("${item.cost}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Coin(18)
        }
    }
}

// ---------------------------------------------------------------------------
// Skins shop
// ---------------------------------------------------------------------------
@Composable
fun SkinShopDialog(
    shop: ShopState,
    onBuy: (Skin) -> Unit,
    onEquip: (Skin) -> Unit,
    onAdminCode: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var codeMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = PanelColor) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogTitle(R.drawable.ic_skins, "السكنات", shop.balance)
                Text(
                    "بدل شكل الشخصية. السكن اللي تشريه كيبقى ديالك، وتقدر تبدل بين السكنات فأي وقت.",
                    color = MutedText,
                    fontSize = 13.sp
                )
                for (skin in Skin.values()) {
                    SkinRow(skin = skin, shop = shop, onBuy = onBuy, onEquip = onEquip)
                }

                // Admin code
                Text("رمز الأدمن", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = code,
                        onValueChange = { code = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        cursorBrush = SolidColor(Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardColor)
                            .padding(14.dp)
                    )
                    Button(
                        onClick = {
                            val ok = onAdminCode(code)
                            codeMessage = if (ok) "تم فتح سكن Zynox" else "الرمز غير صحيح"
                            if (ok) code = ""
                        }
                    ) {
                        Text("فتح")
                    }
                }
                if (codeMessage.isNotEmpty()) {
                    Text(codeMessage, color = GoldText, fontSize = 13.sp)
                }

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("إغلاق")
                }
            }
        }
    }
}

@Composable
private fun SkinRow(
    skin: Skin,
    shop: ShopState,
    onBuy: (Skin) -> Unit,
    onEquip: (Skin) -> Unit
) {
    val owned = shop.owns(skin)
    val equipped = shop.selectedSkin == skin.id
    val locked = !owned && skin.adminOnly

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (equipped) Color(0xFF3A2D52) else CardColor)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(104.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF3F3160)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = skin.previewRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .alpha(if (locked) 0.45f else 1f)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(skin.nameEn, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(skin.taglineAr, color = MutedText, fontSize = 12.sp)
        }
        when {
            equipped -> Text("مجهّز", color = Color(0xFF66BB6A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            owned -> Button(onClick = { onEquip(skin) }) { Text("جهّز") }
            skin.adminOnly -> Text("ADMIN", color = Color(0xFFFF4081), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            else -> Button(
                onClick = { onBuy(skin) },
                enabled = shop.balance >= skin.price,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
            ) {
                Text("${skin.price}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Coin(18)
            }
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
        Surface(shape = RoundedCornerShape(24.dp), color = PanelColor) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogTitle(R.drawable.ic_trophy, "الإنجازات", shop.balance)
                Text(
                    "حقق الأهداف واستلم مكونات كمكافأة تقدر تصرفها فالمتجر.",
                    color = MutedText,
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
            .background(CardColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = milestone.iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .alpha(if (done) 1f else 0.45f)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(milestone.titleAr, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(milestone.descAr, color = MutedText, fontSize = 12.sp)
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
                color = MutedText,
                fontSize = 11.sp
            )
        }
        when {
            claimed -> Text("✓ تم", color = Color(0xFF66BB6A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            done -> Button(
                onClick = { onClaim(milestone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Text("+${milestone.reward}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Coin(18)
            }
            else -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("+${milestone.reward}", color = Color(0xFF8E86A0), fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Coin(16)
            }
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
                    .size(54.dp)
                    .alpha(if (active) 1f else 0.45f)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.92f))
                    .clickable(enabled = active) { onUse(item) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
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
