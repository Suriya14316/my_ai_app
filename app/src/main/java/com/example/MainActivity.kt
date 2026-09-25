package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AssistantState
import com.example.data.model.SayaScreen
import com.example.data.model.UserTask
import com.example.data.model.VoiceProfile
import com.example.ui.SayaViewModel
import com.example.ui.components.InfoDialog
import com.example.ui.components.TranscriptLog
import com.example.ui.components.VoiceOrb
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SiriBlue
import com.example.ui.theme.SiriCyan
import com.example.ui.theme.SiriEmerald
import com.example.ui.theme.SiriPink
import com.example.ui.theme.SiriPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

class MainActivity : ComponentActivity() {

    private val viewModel: SayaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                SayaMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SayaMainScreen(viewModel: SayaViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val assistantState by viewModel.assistantState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val rmsVolume by viewModel.rmsVolume.collectAsStateWithLifecycle()
    val isContinuousWakeWordEnabled by viewModel.isContinuousWakeWordEnabled.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val toastEvent by viewModel.toastEvent.collectAsStateWithLifecycle()
    val activeVoice by viewModel.activeVoice.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isVoiceMatchEnabled by viewModel.isVoiceMatchEnabled.collectAsStateWithLifecycle()

    var showInfoDialog by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            Toast.makeText(context, "Audio permission is needed to record voice", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(toastEvent) {
        toastEvent?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SayaBottomNavBar(
                currentScreen = currentScreen,
                onScreenSelected = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Global Header Bar
            GlobalHeaderBar(
                currentScreen = currentScreen,
                assistantState = assistantState,
                isWakeWordActive = isContinuousWakeWordEnabled,
                activeVoice = activeVoice,
                onToggleWakeWord = { viewModel.toggleContinuousWakeWord() },
                onToggleVoice = { viewModel.toggleVoiceProfile() },
                onOpenInfo = { showInfoDialog = true }
            )

            // Screen Content Switcher
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentScreen) {
                    SayaScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        assistantState = assistantState,
                        rmsVolume = rmsVolume,
                        messages = messages,
                        liveTranscript = liveTranscript,
                        hasAudioPermission = hasAudioPermission,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                    SayaScreen.TASKS -> TasksScreen(
                        tasks = tasks,
                        onToggleTask = { id -> viewModel.toggleTask(id) },
                        onAddTask = { title -> viewModel.addTask(title) },
                        onDeleteTask = { id -> viewModel.deleteTask(id) },
                        onVoiceCommand = { cmd -> viewModel.processCommand(cmd) }
                    )
                    SayaScreen.CONTROLS -> ControlsScreen(
                        onTriggerCommand = { cmd -> viewModel.processCommand(cmd) }
                    )
                    SayaScreen.SETTINGS -> SettingsScreen(
                        activeVoice = activeVoice,
                        isVoiceMatchEnabled = isVoiceMatchEnabled,
                        customApiKey = customApiKey,
                        onToggleVoice = { viewModel.toggleVoiceProfile() },
                        onToggleVoiceMatch = { viewModel.toggleVoiceMatch() },
                        onSaveApiKey = { key -> viewModel.saveCustomApiKey(key) },
                        onOpenInfo = { showInfoDialog = true }
                    )
                }
            }

            // Universal Bottom Mic Action Bar
            UniversalMicBar(
                assistantState = assistantState,
                manualInputText = manualInputText,
                onManualInputChanged = { manualInputText = it },
                onSendManualInput = {
                    if (manualInputText.isNotBlank()) {
                        viewModel.processCommand(manualInputText)
                        manualInputText = ""
                    }
                },
                onMicClicked = {
                    if (!hasAudioPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.toggleListening()
                    }
                }
            )
        }
    }

    if (showInfoDialog) {
        InfoDialog(
            currentApiKey = customApiKey,
            onSaveApiKey = { newKey ->
                viewModel.saveCustomApiKey(newKey)
            },
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun GlobalHeaderBar(
    currentScreen: SayaScreen,
    assistantState: AssistantState,
    isWakeWordActive: Boolean,
    activeVoice: VoiceProfile,
    onToggleWakeWord: () -> Unit,
    onToggleVoice: () -> Unit,
    onOpenInfo: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SAYA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SiriPurple.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentScreen.title.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiriPurple
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Voice Switcher chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, SiriCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onToggleVoice() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("voice_profile_toggle")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = SiriCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeVoice.shortName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SiriCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Wake Word toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWakeWordActive) SiriEmerald.copy(alpha = 0.18f) else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isWakeWordActive) SiriEmerald.copy(alpha = 0.5f) else DarkSurfaceBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onToggleWakeWord() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isWakeWordActive) "● Wake Active" else "○ Standby",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isWakeWordActive) SiriEmerald else TextTertiary
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenInfo,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, CircleShape)
                        .testTag("info_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Project Info",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Status Text
        AnimatedContent(
            targetState = assistantState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status_text_transition"
        ) { state ->
            val statusColor = when (state) {
                AssistantState.IDLE -> TextSecondary
                AssistantState.ACTIVATED -> SiriEmerald
                AssistantState.LISTENING -> SiriCyan
                AssistantState.THINKING -> SiriPurple
                AssistantState.SPEAKING -> SiriPink
                AssistantState.ERROR -> Color(0xFFEF4444)
            }

            Text(
                text = state.statusLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = statusColor,
                modifier = Modifier.testTag("dynamic_status_text")
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: SayaViewModel,
    assistantState: AssistantState,
    rmsVolume: Float,
    messages: List<com.example.data.model.ChatMessage>,
    liveTranscript: String,
    hasAudioPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center Voice Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            VoiceOrb(
                state = assistantState,
                rmsVolume = rmsVolume,
                onClick = {
                    if (!hasAudioPermission) {
                        onRequestPermission()
                    } else {
                        viewModel.toggleListening()
                    }
                },
                size = 165.dp
            )
        }

        // Quick Navigation and Activation Chips
        QuickActionChips(
            onSelectChip = { command ->
                viewModel.processCommand(command)
            }
        )

        // Chat Transcript
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TranscriptLog(
                messages = messages,
                liveTranscript = liveTranscript,
                onReplaySpeech = { text ->
                    viewModel.replaySpeech(text)
                }
            )
        }
    }
}

@Composable
fun TasksScreen(
    tasks: List<UserTask>,
    onToggleTask: (String) -> Unit,
    onAddTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onVoiceCommand: (String) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Siri Tasks & Reminders",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Say \"Add task [name]\" or \"Show my tasks\" from anywhere",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                placeholder = { Text("Add task or say 'Add task...'", fontSize = 12.sp, color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiriCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        onAddTask(newTaskTitle)
                        newTaskTitle = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(SiriBlue)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Task",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Voice quick test chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Show my tasks", "Add task prepare presentation", "Navigate to home").forEach { chip ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { onVoiceCommand(chip) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = chip, fontSize = 11.sp, color = SiriCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Task List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(tasks, key = { it.id }) { task ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) DarkSurfaceElevated.copy(alpha = 0.5f) else DarkSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (task.isCompleted) SiriEmerald else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (task.isCompleted) SiriEmerald else SiriCyan.copy(alpha = 0.7f),
                                    CircleShape
                                )
                                .clickable { onToggleTask(task.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = task.title,
                            fontSize = 13.sp,
                            color = if (task.isCompleted) TextTertiary else TextPrimary,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onDeleteTask(task.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlsScreen(onTriggerCommand: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Smart Controls & Shortcuts",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Trigger native Android actions or routines hands-free",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Welcome Routine Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SiriEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onTriggerCommand("weak dady is home") }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SiriEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        tint = SiriEmerald,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Trigger \"Weak dady is home\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SiriEmerald
                    )
                    Text(
                        text = "Runs Welcome Home routine, task report & security check",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Quick Actions
        val actions = listOf(
            Triple("Open WhatsApp", Icons.AutoMirrored.Rounded.Send, "open whatsapp"),
            Triple("Open YouTube", Icons.Rounded.VideoLibrary, "open youtube"),
            Triple("Weather Report", Icons.Rounded.Cloud, "weather today"),
            Triple("What is the Time", Icons.Rounded.Tune, "what is the time"),
            Triple("Call Contact", Icons.Rounded.Phone, "call Mom"),
            Triple("Google Search", Icons.Rounded.Search, "search news on google")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (label, icon, cmd) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
                                .clickable { onTriggerCommand(cmd) }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = SiriCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    activeVoice: VoiceProfile,
    isVoiceMatchEnabled: Boolean,
    customApiKey: String,
    onToggleVoice: () -> Unit,
    onToggleVoiceMatch: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onOpenInfo: () -> Unit
) {
    var keyInput by remember { mutableStateOf(customApiKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Voice & AI Settings",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Configure dual voices, voice security, and Gemini keys",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dual Voice Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Selected Voice Profile",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiriCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeVoice.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = activeVoice.description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onToggleVoice,
                    colors = ButtonDefaults.buttonColors(containerColor = SiriBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Switch to ${if (activeVoice == VoiceProfile.VOICE_1_IRIS) "Voice 2 (Orion)" else "Voice 1 (Iris)"}")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Voice Match Security Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Match ID",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiriEmerald
                    )
                    Text(
                        text = if (isVoiceMatchEnabled) "Recognizing: Primary User (Daddy) ✓" else "Voice Match Disabled",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isVoiceMatchEnabled) SiriEmerald.copy(alpha = 0.2f) else DarkBackground)
                        .clickable { onToggleVoiceMatch() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isVoiceMatchEnabled) "Enabled" else "Disabled",
                        fontSize = 11.sp,
                        color = if (isVoiceMatchEnabled) SiriEmerald else TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gemini Key config
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Gemini API Key",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiriPurple
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    placeholder = { Text("Paste Gemini Key (optional)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SiriCyan,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onSaveApiKey(keyInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = SiriPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Key")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onOpenInfo,
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open College Presentation & Architecture Guide")
        }
    }
}

@Composable
fun SayaBottomNavBar(
    currentScreen: SayaScreen,
    onScreenSelected: (SayaScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0C0E17),
        contentColor = TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val items = listOf(
            Pair(SayaScreen.HOME, Icons.Rounded.Home),
            Pair(SayaScreen.TASKS, Icons.AutoMirrored.Rounded.FormatListBulleted),
            Pair(SayaScreen.CONTROLS, Icons.Rounded.Tune),
            Pair(SayaScreen.SETTINGS, Icons.Rounded.Settings)
        )

        items.forEach { (screen, icon) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) SiriCyan else TextTertiary
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SiriCyan else TextTertiary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SiriCyan.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun QuickActionChips(onSelectChip: (String) -> Unit) {
    val chips = listOf(
        "Weak dady is home",
        "Hey SAYA",
        "Navigate to tasks",
        "Navigate to controls",
        "Navigate to settings",
        "Show my tasks",
        "Add task prepare dinner",
        "Switch voice",
        "Open WhatsApp",
        "Weather today",
        "What is the time",
        "Open YouTube",
        "Call Mom"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chipText ->
            val isActivationChip = chipText == "Weak dady is home" || chipText == "Hey SAYA"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActivationChip) SiriEmerald.copy(alpha = 0.2f) else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (isActivationChip) SiriEmerald.copy(alpha = 0.6f) else DarkSurfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectChip(chipText) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("chip_${chipText.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = chipText,
                    fontSize = 12.sp,
                    color = if (isActivationChip) SiriEmerald else Color(0xFFCBD5E1),
                    fontWeight = if (isActivationChip) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun UniversalMicBar(
    assistantState: AssistantState,
    manualInputText: String,
    onManualInputChanged: (String) -> Unit,
    onSendManualInput: () -> Unit,
    onMicClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = manualInputText,
                onValueChange = onManualInputChanged,
                placeholder = {
                    Text(
                        text = "Command e.g. \"Navigate to tasks\"...",
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendManualInput() }),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiriCyan,
                    unfocusedBorderColor = DarkSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("manual_text_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendManualInput,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (manualInputText.isNotBlank()) SiriBlue else DarkSurfaceElevated)
                    .border(1.dp, DarkSurfaceBorder, CircleShape)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send Command",
                    tint = if (manualInputText.isNotBlank()) Color.White else TextTertiary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val isListening = assistantState == AssistantState.LISTENING
        val isSpeaking = assistantState == AssistantState.SPEAKING
        val isActivated = assistantState == AssistantState.ACTIVATED

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = when {
                            isActivated -> listOf(SiriEmerald, SiriCyan)
                            isListening -> listOf(SiriCyan, SiriBlue)
                            isSpeaking -> listOf(SiriPink, SiriPurple)
                            else -> listOf(DarkSurfaceElevated, Color(0xFF1E2638))
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    color = when {
                        isActivated -> SiriEmerald
                        isListening -> SiriCyan
                        isSpeaking -> SiriPink
                        else -> SiriBlue.copy(alpha = 0.5f)
                    },
                    shape = CircleShape
                )
                .clickable { onMicClicked() }
                .testTag("mic_toggle_button")
        ) {
            Icon(
                imageVector = when {
                    isListening -> Icons.Rounded.GraphicEq
                    isSpeaking -> Icons.Rounded.Stop
                    else -> Icons.Rounded.Mic
                },
                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                tint = if (isActivated || isListening || isSpeaking) Color.White else SiriCyan,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
