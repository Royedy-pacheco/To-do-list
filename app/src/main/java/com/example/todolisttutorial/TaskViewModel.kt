package com.example.todolisttutorial

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    var taskItems = MutableLiveData<MutableList<TaskItem>>()

    private val preferences: SharedPreferences = application.getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    init {
        val storedTasks = loadTasksFromPreferences()
        taskItems.value = storedTasks.toMutableList()
    }

    private fun loadTasksFromPreferences(): List<TaskItem> {
        val tasksString = preferences.getString("taskItems", null)
        return if (tasksString != null) {
            val tasks = Gson().fromJson<List<TaskItem>>(tasksString, object : TypeToken<List<TaskItem>>() {}.type)
            tasks
        } else {
            emptyList()
        }
    }

    private fun saveTasksToPreferences() {
        val tasksString = Gson().toJson(taskItems.value)
        editor.putString("taskItems", tasksString)
        editor.apply()
    }

    fun addTaskItem(newTask: TaskItem) {
        val list = taskItems.value
        list?.add(newTask)
        taskItems.postValue(list)
        saveTasksToPreferences()
    }

    fun updateTaskItem(id: UUID, name: String, desc: String, dueTime: LocalTime?) {
        val list = taskItems.value
        val task = list?.find { it.id == id }
        task?.let {
            it.name = name
            it.desc = desc
            it.dueTime = dueTime
        }
        taskItems.postValue(list)
        saveTasksToPreferences()
    }

    fun setCompleted(taskItem: TaskItem) {
        val list = taskItems.value
        val task = list?.find { it.id == taskItem.id }
        task?.let {
            if (it.completedDate == null) {
                it.completedDate = LocalDate.now()
                taskItems.postValue(list)

                // Agregar un retraso de 1 segundo
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    // Eliminar la tarea de la lista después del retraso
                    list?.remove(it)
                    taskItems.postValue(list)
                    saveTasksToPreferences()
                }, 1000)
            }
        }
    }
}



