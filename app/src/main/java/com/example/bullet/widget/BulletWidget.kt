package com.example.bullet.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.bullet.MainActivity
import com.example.bullet.R
import com.example.bullet.data.db.Task
import com.example.bullet.data.db.TaskStatus
import com.example.bullet.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val SIZE_SMALL = DpSize(110.dp, 110.dp)
private val SIZE_LARGE = DpSize(250.dp, 110.dp)

private val openAction = actionStartActivity<MainActivity>()

// ── Theme-resolved colours (call once per provideGlance) ─────────────────────
private data class WidgetColors(
    val bg: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val iconRes: Int,
)

private fun resolveColors(context: Context): WidgetColors {
    val isNight = (context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    return if (isNight) WidgetColors(
        bg            = Color(0xFF141414),
        surface       = Color(0xFF1E1E1E),
        textPrimary   = Color(0xFFF2EDE4),
        textSecondary = Color(0xFF9A9590),
        divider       = Color(0xFF2E2E2E),
        iconRes       = R.drawable.nib_icon_white,
    ) else WidgetColors(
        bg            = Color(0xFFF2EDE4),
        surface       = Color(0xFFF7F3EC),
        textPrimary   = Color(0xFF1A1A1A),
        textSecondary = Color(0xFF6B6560),
        divider       = Color(0xFFD4CFC6),
        iconRes       = R.drawable.nib_icon_black,
    )
}

class NibWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SIZE_SMALL, SIZE_LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .taskRepository()

        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dayOfWeek = today.format(DateTimeFormatter.ofPattern("EEEE")).uppercase()
        val monthDay = today.format(DateTimeFormatter.ofPattern("MMM d"))

        repository.migratePastOpenTasks()
        repository.generateRecurringTasksForToday()

        val openTasks = repository
            .getTasksForDate(todayStr)
            .first()
            .filter { it.status != TaskStatus.CLOSED }

        val aspirations = repository.getAllAspirations().first()
        val dailyAspiration: String? = if (aspirations.isEmpty()) null else {
            val index = (today.toEpochDay() % aspirations.size).toInt()
            aspirations[index].title
        }

        val colors = resolveColors(context)

        provideContent {
            GlanceTheme {
                if (LocalSize.current.width < SIZE_LARGE.width) {
                    SmallWidget(
                        dayOfWeek = dayOfWeek,
                        monthDay = monthDay,
                        openCount = openTasks.size,
                        aspiration = dailyAspiration,
                        colors = colors,
                    )
                } else {
                    LargeWidget(
                        dayOfWeek = dayOfWeek,
                        monthDay = monthDay,
                        tasks = openTasks,
                        aspiration = dailyAspiration,
                        colors = colors,
                    )
                }
            }
        }
    }
}

// ── 2×2 compact ──────────────────────────────────────────────────────────────

@Composable
private fun SmallWidget(
    dayOfWeek: String,
    monthDay: String,
    openCount: Int,
    aspiration: String?,
    colors: WidgetColors,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(colors.bg))
            .cornerRadius(20.dp)
            .clickable(openAction)
            .padding(14.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (aspiration != null) {
            Text(
                text = "\" $aspiration \"",
                style = TextStyle(
                    color = ColorProvider(colors.textSecondary),
                    fontSize = 9.sp,
                    fontStyle = FontStyle.Italic,
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth(),
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
        }
        Text(
            text = dayOfWeek,
            style = TextStyle(
                color = ColorProvider(colors.textSecondary),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = monthDay,
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(6.dp))
        Text(
            text = if (openCount == 0) "All done" else "$openCount open",
            style = TextStyle(
                color = ColorProvider(
                    if (openCount == 0) colors.textSecondary else colors.textPrimary
                ),
                fontSize = 11.sp,
            ),
        )
    }
}

// ── Full scrollable ───────────────────────────────────────────────────────────

@Composable
private fun LargeWidget(
    dayOfWeek: String,
    monthDay: String,
    tasks: List<Task>,
    aspiration: String?,
    colors: WidgetColors,
) {
    LazyColumn(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(colors.bg))
            .cornerRadius(20.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        // Header: aspiration + [date | logo] row
        item {
            Column(modifier = GlanceModifier.fillMaxWidth().clickable(openAction)) {
                if (aspiration != null) {
                    Text(
                        text = "\" $aspiration \"",
                        style = TextStyle(
                            color = ColorProvider(colors.textSecondary),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                        ),
                        maxLines = 2,
                        modifier = GlanceModifier.fillMaxWidth(),
                    )
                    Spacer(modifier = GlanceModifier.height(14.dp))
                }
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = dayOfWeek,
                            style = TextStyle(
                                color = ColorProvider(colors.textSecondary),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Text(
                            text = monthDay,
                            style = TextStyle(
                                color = ColorProvider(colors.textPrimary),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    Image(
                        provider = ImageProvider(colors.iconRes),
                        contentDescription = null,
                        modifier = GlanceModifier.size(30.dp),
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(10.dp))
        }

        item {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(colors.divider))
                    .clickable(openAction),
            ) {}
            Spacer(modifier = GlanceModifier.height(6.dp))
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    text = "Nothing left to do",
                    style = TextStyle(
                        color = ColorProvider(colors.textSecondary),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    modifier = GlanceModifier.clickable(openAction),
                )
            }
        } else {
            items(tasks) { task -> WidgetTaskRow(task.content, colors) }
        }
    }
}

@Composable
private fun WidgetTaskRow(content: String, colors: WidgetColors) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(openAction),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "·",
            style = TextStyle(
                color = ColorProvider(colors.textPrimary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = content,
            style = TextStyle(color = ColorProvider(colors.textPrimary), fontSize = 13.sp),
            maxLines = 1,
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}
