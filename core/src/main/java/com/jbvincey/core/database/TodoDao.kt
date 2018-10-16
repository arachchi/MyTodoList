package com.jbvincey.core.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.jbvincey.core.models.Todo

/**
 * Created by jbvincey on 16/09/2018.
 */
@Dao
interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodo(todo: Todo)

    @Delete
    fun deleteTodo(todo: Todo)

    @Query("SELECT * FROM todos")
    fun getAllTodos(): LiveData<List<Todo>>

    @Query("SELECT * FROM todos WHERE id = :id")
    fun getTodoById(id: Long): LiveData<Todo>

    @Query("UPDATE todos Set completed = NOT completed WHERE id = :id")
    fun changeTodoCompleted(id: Long)

}