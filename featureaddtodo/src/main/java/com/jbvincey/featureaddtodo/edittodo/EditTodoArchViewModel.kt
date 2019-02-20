package com.jbvincey.featureaddtodo.edittodo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.jbvincey.core.models.Todo
import com.jbvincey.core.repositories.TodoRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by jbvincey on 27/10/2018.
 */
class EditTodoArchViewModel(private val todoRepository: TodoRepository): ViewModel() {

    private val disposables = CompositeDisposable()

    val todoId: MutableLiveData<Long> = MutableLiveData()
    val todo: LiveData<Todo> = Transformations.switchMap(todoId) { todoId ->
        todoRepository.getTodoById(todoId)
    }
    val editTodoState = MutableLiveData<EditTodoState>()
    val deleteTodoState = MutableLiveData<DeleteTodoState>()
    val archiveTodoState = MutableLiveData<ArchiveTodoState>()
    val unarchiveTodoState = MutableLiveData<UnarchiveTodoState>()

    fun editTodo(todoName: String) {
        disposables.add(todoRepository.editTodo(todoName, todoId.value!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { editTodoState.value = EditTodoState.Success },
                        { editTodoState.value = EditTodoState.UnknownError }
                ))
    }

    fun deleteTodo() {
        disposables.add(todoRepository.deleteTodo(todoId.value!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { deleteTodoState.value = DeleteTodoState.Success },
                        { deleteTodoState.value = DeleteTodoState.UnknownError }
                ))
    }

    fun archiveTodo(displaySnackOnSuccess: Boolean) {
        disposables.add(todoRepository.archiveTodo(todoId.value!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { archiveTodoState.value = ArchiveTodoState.Success(displaySnackOnSuccess, todo.value!!.name) },
                        { archiveTodoState.value = ArchiveTodoState.UnknownError }
                ))
    }

    fun unarchiveTodo(displaySnackOnSuccess: Boolean) {
        disposables.add(todoRepository.unarchiveTodo(todoId.value!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { unarchiveTodoState.value = UnarchiveTodoState.Success(displaySnackOnSuccess, todo.value!!.name) },
                        { unarchiveTodoState.value = UnarchiveTodoState.UnknownError }
                ))
    }

    fun shouldShowArchiveMenu(): Boolean {
        val todo = todo.value
        return todo?.completed == true && !todo.archived
    }

    fun shouldShowUnarchiveMenu(): Boolean {
        val todo = todo.value
        return todo?.completed == true && todo.archived
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}

sealed class EditTodoState {
    object Success : EditTodoState()
    object UnknownError : EditTodoState()
}

sealed class DeleteTodoState {
    object Success : DeleteTodoState()
    object UnknownError : DeleteTodoState()
}

sealed class ArchiveTodoState {
    class Success(val displaySnack: Boolean, val todoName: String) : ArchiveTodoState()
    object UnknownError : ArchiveTodoState()
}

sealed class UnarchiveTodoState {
    class Success(val displaySnack: Boolean, val todoName: String) : UnarchiveTodoState()
    object UnknownError : UnarchiveTodoState()
}