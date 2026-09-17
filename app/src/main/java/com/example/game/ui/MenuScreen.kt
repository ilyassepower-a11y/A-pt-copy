package com.example.game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.data.GameSaveData
import com.example.game.haptics.HapticsManager
import com.example.game.model.GameDifficulty

@Composable
fun MenuScreen(
    saveData: GameSaveData,
    haptics: HapticsManager,
    onStartGame: (GameDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.CLASSIC) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF261019),
                        Color(0xFF3E121E),
                        Color(0xFF1B070D)
                    )
                )
            )
    ) {
        // Decorative background image if available
        Image(
            painter = painterResource(id = R.drawable.pizza_tower_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.22f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Icon / Hero Avatar
            Surface(
                modifier = Modifier
                    .size(90.dp)
                    .border(3.dp, Color(0xFFFFB300), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE65100)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pizza_escape_icon),
                    contentDescription = "App Icon",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Title (Pizza Tower inspired)
            Text(
                text = "PIZZA ESCAPE",
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Text(
                text = "هروب برج البيتزا",
                color = Color(0xFFFF7043),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Objective Tagline
            Surface(
                color = Color(0xCC000000),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.border(1.dp, Color(0xFFFF5722), RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = "عليك الهروب حتى ينفذ الوقت من المطارد!\n(Survive until countdown ends!)",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Difficulty Selector Cards
            Text(
                text = "اختر مستوى الهروب (Escape Mode)",
                color = Color(0xFFFFE082),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameDifficulty.values().forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    Card(
                        onClick = {
                            haptics.lightTap()
                            selectedDifficulty = diff
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFD84315) else Color(0xDD2D1620)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFD54F) else Color(0x44FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("mode_${diff.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (diff) {
                                    GameDifficulty.SPRINT -> Icons.Default.Bolt
                                    GameDifficulty.CLASSIC -> Icons.Default.LocalFireDepartment
                                    GameDifficulty.PANIC -> Icons.Default.Timer
                                },
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFFFFB74D),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = diff.titleAr,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${diff.durationSeconds.toInt()}s",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // START ESCAPE BUTTON
            Button(
                onClick = {
                    haptics.impactHeavy()
                    onStartGame(selectedDifficulty)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(2.5.dp, Color(0xFF81C784), RoundedCornerShape(16.dp))
                    .testTag("start_escape_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ابدأ الهروب (START ESCAPE)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // High Score & Stats Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xCC1A0C14)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFB300), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "السجلات والإحصائيات",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        if (saveData.bestRank != "-") {
                            Surface(
                                color = Color(0xFFD50000),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "رتبة ${saveData.bestRank} RANK",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "أعلى نقاط: ${saveData.highScore}",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "مرات الهروب الناجحة: ${saveData.totalEscapes}",
                            color = Color(0xFF81C784),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مجموع المكونات المجمعة: ${saveData.totalToppings}",
                            color = Color(0xFFFFB74D),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "إجمالي المحاولات: ${saveData.totalGames}",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // How to Play Quick Tips
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xBB160C14)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "كيف تلعب؟ (How to Play)",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• استمر بالركض واضغط MACH للوصول إلى السرعة القصوى واختراق حواجز القشرة!\n" +
                                "• اقفز على الجدران للقفز العكسي للأعلى (Wall Jump).\n" +
                                "• اضغط SLAM للهبوط السريع وكسر المنصات للأسفل.\n" +
                                "• اضغط PARRY عند اقتراب المطارد لصعقه وإبعاده!\n" +
                                "• اجمع الفلفل الحار لإشعال سرعة Mach 4 وتجميد المطارد.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
