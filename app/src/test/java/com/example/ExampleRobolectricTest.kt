package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SayaScreen
import com.example.data.model.UserTask
import com.example.data.remote.WeatherClient
import com.example.service.CommandResult
import com.example.service.CommandRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SAYA", appName)
  }

  @Test
  fun `verify weak dady is home activation command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val tasks = listOf(UserTask(title = "Review schedule"))
    val result = router.route("weak dady is home", tasks = tasks)
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Voice Activation", immediate.actionBadge)
    assertTrue(immediate.isActivation)
    assertTrue(immediate.speechText.contains("Yes, boss!"))
    assertTrue(immediate.speechText.contains("Welcome home"))
  }

  @Test
  fun `verify hey saya activation command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val result = router.route("hey saya")
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Voice Activation", immediate.actionBadge)
    assertTrue(immediate.isActivation)
    assertTrue(immediate.speechText.contains("Yes, boss!"))
  }

  @Test
  fun `verify voice navigation to tasks screen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val result = router.route("navigate to tasks")
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Navigation", immediate.actionBadge)
    assertEquals(SayaScreen.TASKS, immediate.navigateToScreen)
    assertTrue(immediate.speechText.contains("Yes, boss!"))
  }

  @Test
  fun `verify voice navigation to home screen`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val result = router.route("go home")
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Navigation", immediate.actionBadge)
    assertEquals(SayaScreen.HOME, immediate.navigateToScreen)
    assertTrue(immediate.speechText.contains("Yes, boss!"))
  }

  @Test
  fun `verify siri task creation command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    var addedTitle = ""
    val result = router.route("add task prepare dinner", onAddTask = { title ->
      addedTitle = title
    })
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Siri Tasks", immediate.actionBadge)
    assertEquals("prepare dinner", addedTitle)
  }

  @Test
  fun `verify siri show tasks command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val tasks = listOf(UserTask(title = "Turn off lights"))
    val result = router.route("show my tasks", tasks = tasks)
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("Siri Tasks", immediate.actionBadge)
    assertTrue(immediate.displayText.contains("Turn off lights"))
  }

  @Test
  fun `verify command router whatsapp routing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val router = CommandRouter(context)
    val result = router.route("open whatsapp")
    assertTrue(result is CommandResult.Immediate)
    val immediate = result as CommandResult.Immediate
    assertEquals("WhatsApp Intent", immediate.actionBadge)
  }

  @Test
  fun `verify weather condition code mapping`() {
    assertEquals("Clear skies", WeatherClient.weatherCodeToCondition(0))
    assertEquals("Partly cloudy", WeatherClient.weatherCodeToCondition(2))
    assertEquals("Thunderstorm activity", WeatherClient.weatherCodeToCondition(95))
  }
}
