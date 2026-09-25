package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SiriBlue
import com.example.ui.theme.SiriCyan
import com.example.ui.theme.SiriEmerald
import com.example.ui.theme.SiriPink
import com.example.ui.theme.SiriPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Info & Presentation Dialog explaining the technical architecture,
 * dual activation commands ("Weak dady is home", "Hey SAYA"),
 * dual voice profiles, Siri task integration, and Android vs Web trade-offs.
 */
@Composable
fun InfoDialog(
    currentApiKey: String,
    onSaveApiKey: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var keyInput by remember { mutableStateOf(currentApiKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp))
                .testTag("info_dialog_surface"),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SiriPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Smartphone,
                                contentDescription = null,
                                tint = SiriPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SAYA Project Guide",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "College Presentation & Architecture",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dual Activation Commands Note
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Dual Activation Commands",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = SiriEmerald
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. \"Weak dady is home\" / \"Wake daddy is home\": Triggers full active mode, announces personalized welcome home routine, reviews pending Siri tasks, and checks home status.\n" +
                                    "2. \"Hey SAYA\": Wakes the assistant for instant task listening.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dual Voice System Note
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Dual Voice Profile System",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = SiriCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Voice 1 (Iris): Warm, friendly Siri-inspired female voice.\n" +
                                    "• Voice 2 (Orion): Deep, resonant Siri-inspired male voice.\n" +
                                    "Switch between them anytime via the top voice chip or say \"switch voice\".",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Architecture Comparison (Browser vs Native Android)
                Text(
                    text = "Browser vs Native Android Architecture",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ComparisonItem(
                    feature = "Wake-Word Detection",
                    browserLimitation = "Web Speech API times out; requires foreground simulated restart loop.",
                    nativeAdvantage = "Native Android can run a low-power Foreground Service with Porcupine/Vosk wake-word DSP even with screen off."
                )

                ComparisonItem(
                    feature = "Calling ('call Mom')",
                    browserLimitation = "Web browsers cannot place phone calls without user dialer confirmation.",
                    nativeAdvantage = "Android supports Intent.ACTION_DIAL (safe dialer) or CALL_PHONE permission for instant dialing."
                )

                ComparisonItem(
                    feature = "Messaging ('send message to Bob')",
                    browserLimitation = "Browser cannot read contacts; relies on wa.me URL redirects.",
                    nativeAdvantage = "Android accesses ContactsContract and directly dispatches SMS via Intent.ACTION_SENDTO."
                )

                ComparisonItem(
                    feature = "Siri Tasks & Reminders",
                    browserLimitation = "Web storage limited to LocalStorage without background sync.",
                    nativeAdvantage = "Android persists tasks in Room SQLite database with AlarmManager / WorkManager reminders."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gemini API Configuration
                Text(
                    text = "Gemini API Configuration",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configured via AI Studio Secrets Panel or provide a custom key below for live demo testing:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("Paste Gemini API Key (optional)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SiriCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onSaveApiKey(keyInput)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiriBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save & Apply")
                }
            }
        }
    }
}

@Composable
fun ComparisonItem(
    feature: String,
    browserLimitation: String,
    nativeAdvantage: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = feature,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = SiriPink
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Web limitation: $browserLimitation",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = SiriCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Native Android: $nativeAdvantage",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp
                )
            }
        }
    }
}
