package com.example.smartstudy1

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartstudy1.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainActivity : ComponentActivity() {

    data class User(val uid: String, val name: String, val email: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val datumiIso = getThisWeekIsoDatesLocal()
            val todayIso = todayIsoLocal()
            var selektiraniDatumIso by remember {
                mutableStateOf(datumiIso.find { it == todayIso } ?: datumiIso.first())
            }

            var currentUser by remember { mutableStateOf<User?>(null) }
            var tasks by remember { mutableStateOf(listOf<Task>()) }

            LaunchedEffect(Unit) {
                val fu = FirebaseAuth.getInstance().currentUser
                currentUser = if (fu != null)
                    User(fu.uid, fu.displayName ?: fu.email ?: "Korisnik", fu.email ?: "")
                else null
            }

            // 🔁 Listener – čita effectiveDone = isDone || done, normalizira id, spaja duplikate i auto-heal-a BAZU (oba polja)
            DisposableEffect(currentUser?.uid) {
                val uid = currentUser?.uid
                if (uid == null) {
                    tasks = emptyList()
                    onDispose { }
                } else {
                    val ref = FirebaseDatabase.getInstance(
                        "https://smartstudyapp-c078c-default-rtdb.europe-west1.firebasedatabase.app/"
                    ).getReference("tasks").child(uid)

                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val all = mutableListOf<Task>()

                            for (child in snapshot.children) {
                                val key = child.key ?: continue
                                val title = child.child("title").getValue(String::class.java) ?: ""
                                val subject = child.child("subject").getValue(String::class.java) ?: ""
                                val date = child.child("date").getValue(String::class.java) ?: ""
                                val isDoneDb = child.child("isDone").getValue(Boolean::class.java) ?: false
                                val doneDb   = child.child("done").getValue(Boolean::class.java) ?: false
                                val effectiveDone = isDoneDb || doneDb


                                if ((child.child("id").getValue(String::class.java) ?: "").isBlank()) {
                                    ref.child(key).child("id").setValue(key)
                                }

                                all.add(Task(id = key, title = title, subject = subject, date = date, isDone = effectiveDone))

                                // uskladi oba statusna polja (ako su različita)
                                if (isDoneDb != effectiveDone || doneDb != effectiveDone) {
                                    ref.child(key).updateChildren(mapOf("isDone" to effectiveDone, "done" to effectiveDone))
                                }
                            }

                            fun contentKey(t: Task) =
                                "${t.title.trim().lowercase()}|${t.subject.trim().lowercase()}|${t.date.trim()}"

                            val groups = all.groupBy(::contentKey)
                            val merged = mutableListOf<Task>()

                            for ((_, list) in groups) {
                                val anyDone = list.any { it.isDone }
                                merged += list.first().copy(isDone = anyDone)


                                list.forEach {
                                    ref.child(it.id).updateChildren(mapOf("isDone" to anyDone, "done" to anyDone))
                                }
                            }
                            tasks = merged
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MainActivity", "Firebase greška: ${error.message}")
                        }
                    }
                    ref.addValueEventListener(listener)
                    onDispose { ref.removeEventListener(listener) }
                }
            }


            fun setTaskDone(taskId: String, value: Boolean) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                if (taskId.isBlank()) return
                val dbRef = FirebaseDatabase.getInstance(
                    "https://smartstudyapp-c078c-default-rtdb.europe-west1.firebasedatabase.app/"
                ).getReference("tasks").child(uid)

                val clicked = tasks.find { it.id == taskId } ?: return
                fun contentKey(t: Task) =
                    "${t.title.trim().lowercase()}|${t.subject.trim().lowercase()}|${t.date.trim()}"
                val key = contentKey(clicked)
                val ids = tasks.filter { contentKey(it) == key }.map { it.id }


                tasks = tasks.map { if (contentKey(it) == key) it.copy(isDone = value) else it }


                val patch = mapOf("isDone" to value, "done" to value)
                ids.forEach { id -> dbRef.child(id).updateChildren(patch) }
            }

            NavHost(navController = navController, startDestination = "welcome") {

                composable("welcome") {
                    WelcomeScreen(
                        onLoginClick = { navController.navigate("login") },
                        onRegisterClick = { navController.navigate("register") }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onBack = { navController.popBackStack() },
                        onLoginSuccess = { fu: FirebaseUser ->
                            currentUser = User(
                                uid = fu.uid,
                                name = fu.displayName ?: fu.email ?: "Korisnik",
                                email = fu.email ?: ""
                            )
                            navController.navigate("dashboard") { popUpTo("welcome") { inclusive = true } }
                        }
                    )
                }

                composable("register") {
                    RegistrationScreen(
                        onBack = { navController.popBackStack() },
                        onRegisterSuccess = {
                            try { FirebaseAuth.getInstance().signOut() } catch (_: Exception) {}
                            currentUser = null
                            navController.navigate("login") { popUpTo("welcome") { inclusive = false }; launchSingleTop = true }
                        }
                    )
                }

                composable("dashboard") {
                    if (currentUser == null) {
                        LaunchedEffect(Unit) {
                            navController.navigate("welcome") { popUpTo("dashboard") { inclusive = true } }
                        }
                    } else {
                        DashboardScreen(
                            imeKorisnika = currentUser?.name ?: "",
                            datumiIsoUTjednu = datumiIso,
                            selektiraniDatumIso = selektiraniDatumIso,
                            zadaciZaDanas = tasks.filter { !it.isDone },
                            userId = currentUser?.uid.orEmpty(),
                            onDanChange = { selektiraniDatumIso = it },
                            naDodajZadatak = { navController.navigate("addTask") },
                            naPregledZavršenih = { navController.navigate("completedTasks") },

                            naMojProfil = { navController.navigate("profile") },
                            naMjesecniKalendar = { navController.navigate("monthCalendar") },
                            onStartTask = { task ->
                                val minutes = durationMinutesFromTask(task)
                                navController.navigate("study/${task.id}/$minutes?from=dashboard")
                            },
                            onTaskChecked = { task, checked -> setTaskDone(task.id, checked) },
                            completedTotal = tasks.count { it.isDone },
                            totalTasksAll = tasks.size
                        )
                    }
                }

                composable(
                    route = "addTask?prefillDate={prefillDate}",
                    arguments = listOf(navArgument("prefillDate") { type = NavType.StringType; nullable = true; defaultValue = "" })
                ) { backStackEntry ->
                    if (currentUser == null) {
                        LaunchedEffect(Unit) { navController.navigate("welcome") { popUpTo("addTask") { inclusive = true } } }
                    } else {
                        val prefillDate = backStackEntry.arguments?.getString("prefillDate") ?: ""
                        AddTaskScreen(
                            onBack = { navController.popBackStack() },
                            onTaskAdded = {
                                navController.navigate("monthCalendar") { popUpTo("addTask") { inclusive = true } }
                            },
                            prefillDate = prefillDate
                        )
                    }
                }

                composable("profile") {
                    if (currentUser == null) {
                        LaunchedEffect(Unit) { navController.navigate("welcome") { popUpTo("profile") { inclusive = true } } }
                    } else {
                        ProfileScreen(
                            ime = currentUser?.name,
                            email = currentUser?.email,
                            brojZavršenih = tasks.count { it.isDone },
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                currentUser = null
                                navController.navigate("welcome") { popUpTo("dashboard") { inclusive = true } }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable("monthCalendar") {
                    if (currentUser == null) {
                        LaunchedEffect(Unit) { navController.navigate("welcome") { popUpTo("monthCalendar") { inclusive = true } } }
                    } else {
                        MonthlyCalendarScreen(
                            tasks = tasks,
                            onAddTask = { selectedDate ->
                                val inFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                val outFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val iso = outFmt.format(inFmt.parse(selectedDate)!!)
                                navController.navigate("addTask?prefillDate=$iso")
                            },
                            onNavigateProfile = { navController.navigate("profile") },
                            onNavigateDashboard = { },
                            onNavigateCompleted = { navController.navigate("completedTasks") },
                            onNavigateCourses = { },
                            onNavigateNext = { navController.navigate("completedTasks") },
                            onNavigatePrev = {
                                val popped = navController.popBackStack()
                                if (!popped) navController.navigate("dashboard") { launchSingleTop = true }
                            },
                            onStartTask = { task ->
                                val minutes = durationMinutesFromTask(task)
                                navController.navigate("study/${task.id}/$minutes?from=monthCalendar")
                            },
                            onTaskChecked = { task, checked -> setTaskDone(task.id, checked) }
                        )
                    }
                }

                composable("completedTasks") {
                    if (currentUser == null) {
                        LaunchedEffect(Unit) { navController.navigate("welcome") { popUpTo("completedTasks") { inclusive = true } } }
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { change, dragAmount ->
                                        change.consumePositionChange()
                                        if (abs(dragAmount) > 80f && dragAmount > 0f) {
                                            val popped = navController.popBackStack()
                                            if (!popped) navController.navigate("monthCalendar") { launchSingleTop = true }
                                        }
                                    }
                                }
                        ) {
                            CompletedTasksScreen(
                                completedTasks = tasks.filter { it.isDone },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }

                composable(
                    route = "study/{taskId}/{minutes}?from={from}",
                    arguments = listOf(
                        navArgument("taskId") { type = NavType.StringType },
                        navArgument("minutes") { type = NavType.IntType },
                        navArgument("from") { type = NavType.StringType; defaultValue = "dashboard"; nullable = true }
                    )
                ) { backStack ->
                    if (currentUser == null) {
                        LaunchedEffect(Unit) { navController.navigate("welcome") { popUpTo("study/{taskId}/{minutes}?from={from}") { inclusive = true } } }
                    } else {
                        val taskId = backStack.arguments?.getString("taskId") ?: ""
                        val minutes = backStack.arguments?.getInt("minutes") ?: 25
                        val task = tasks.find { it.id == taskId }
                        StudySessionScreen(
                            title = task?.title ?: "Učenje",
                            durationMinutes = minutes,
                            onGiveUp = { navController.popBackStack() },
                            onFinished = {
                                setTaskDone(taskId, true)
                                navController.popBackStack()
                                navController.navigate("dashboard") {
                                    launchSingleTop = true
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable("kolegiji") { TextScreen("Kolegiji (placeholder)") }
            }
        }
    }

    private fun getThisWeekIsoDatesLocal(): List<String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return List(7) {
            val d = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            d
        }
    }

    private fun todayIsoLocal(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun durationMinutesFromTask(task: Task): Int {
        val timeRange = task.date.split(" ").getOrNull(1) ?: return 25
        val parts = timeRange.split("-")
        return try {
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = fmt.parse(parts[0].trim())!!.time
            val end = fmt.parse(parts.getOrElse(1) { parts[0] }.trim())!!.time
            (((end - start) / 60000L).toInt()).coerceAtLeast(1)
        } catch (_: Exception) { 25 }
    }
}
