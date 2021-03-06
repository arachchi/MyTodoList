package com.jbvincey.todolist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.jbvincey.design.widget.helper.SwipeCallback
import com.jbvincey.design.widget.helper.SwipeCallbackListener
import com.jbvincey.design.widget.helper.SwipeCallbackModel
import com.jbvincey.navigation.NavigationHandler
import com.jbvincey.ui.appbar.AppbarElevationRecyclerScrollListener
import com.jbvincey.ui.recycler.cells.checkablecell.CheckableCellAdapter
import com.jbvincey.ui.recycler.cells.checkablecell.CheckableCellView
import com.jbvincey.ui.recycler.cells.checkablecell.CheckableCellViewModel
import com.jbvincey.ui.utils.activity.displayActionSnack
import kotlinx.android.synthetic.main.activity_todo_list.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class TodoListActivity : AppCompatActivity() {

    companion object {
        private const val CHECKABLE_CELL_UPDATE_DELAY = 300L
        private const val RECYCLER_TRANSITION_ANIMATION_DURATION = 150L
    }

    private lateinit var recyclerTransitionAnimation: Transition

    private val viewModel: TodoListArchViewModel by viewModel()
    private val navigationHandler: NavigationHandler by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        initView()
    }

    //region view setup

    private fun initView() {
        initToolbar()
        initFabButton()
        initRecycler()
        initBottomNavigation()

        observeTodoClick()
        observeDeleteTodoState()
        observeUndeleteTodoState()
        observeArchiveTodoState()
        observeUnarchiveTodoState()
        viewModel.showUnarchivedTodos()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initFabButton() {
        fabButton.setOnClickListener {
            startActivity(navigationHandler.buildAddTodoIntent(this))
        }
    }

    private fun initRecycler() {

        val swipeCallbackModelStart = SwipeCallbackModel(
                getColor(R.color.colorAccent),
                getDrawable(R.drawable.ic_baseline_delete_white_24px),
                resources.getDimensionPixelSize(R.dimen.swipe_aciton_margin),
                SwipeCallbackListener { view -> onViewSwipedStart(view) }
        )
        val swipeCallbackModelEnd = SwipeCallbackModel(
                getColor(R.color.colorAccent),
                getDrawable(R.drawable.ic_baseline_archive_white_24px),
                resources.getDimensionPixelSize(R.dimen.swipe_aciton_margin),
                SwipeCallbackListener { view -> onViewSwipedEnd(view) }
        )

        val itemTouchHelper = ItemTouchHelper(SwipeCallback(swipeCallbackModelStart, swipeCallbackModelEnd, this))
        itemTouchHelper.attachToRecyclerView(todoRecyclerView)

        recyclerTransitionAnimation = ChangeBounds()
        recyclerTransitionAnimation.duration = RECYCLER_TRANSITION_ANIMATION_DURATION

        todoRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        todoRecyclerView.addOnScrollListener(AppbarElevationRecyclerScrollListener(appbarLayout, todoRecyclerView))

        val adapter = CheckableCellAdapter()
        todoRecyclerView.adapter = adapter
        viewModel.checkableCellViewModelList.observe(this, Observer {
            updateCheckableCellListWithDelay(it, adapter)
        })
    }

    private fun initBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {

                R.id.action_todo_list -> {
                    viewModel.showUnarchivedTodos()
                    true
                }

                R.id.action_archived_list -> {
                    viewModel.showArchivedTodos()
                    true
                }

                else -> false
            }
        }
    }

    //endregion

    //action user actions

    private fun observeTodoClick() {
        viewModel.todoClicked.observe(this, Observer { todoId ->
            startEditActivity(todoId!!)
        })
    }

    private fun updateCheckableCellListWithDelay(checkableCellList: List<CheckableCellViewModel>?,
                                                 adapter: CheckableCellAdapter) {
        lifecycleScope.launch {
            delay(CHECKABLE_CELL_UPDATE_DELAY)
            TransitionManager.beginDelayedTransition(todoRecyclerView, recyclerTransitionAnimation)
            checkableCellList?.let(adapter::submitList)
        }
    }

    private fun onViewSwipedStart(view: View) {
        viewModel.deleteTodo((view as CheckableCellView).getViewModelId())
    }

    private fun observeDeleteTodoState() {
        viewModel.deleteTodoState.observe(this, Observer { state ->
            when (state) {
                is DeleteTodoState.Success -> displayActionSnack(R.string.delete_success, R.string.undo, state.todoName) { viewModel.undeleteTodo(state.todoId) }
                is DeleteTodoState.UnknownError -> displayActionSnack(R.string.error_message, R.string.retry) { viewModel.deleteTodo(state.todoId) }
            }
        })
    }

    private fun observeUndeleteTodoState() {
        viewModel.undeleteTodoState.observe(this, Observer { state ->
            when (state) {
                is UndeleteTodoState.UnknownError -> displayActionSnack(R.string.error_message, R.string.retry) { viewModel.undeleteTodo(state.todoId) }
            }
        })
    }

    private fun onViewSwipedEnd(view: View) {
        viewModel.archiveTodo((view as CheckableCellView).getViewModelId())
    }

    private fun observeArchiveTodoState() {
        viewModel.archiveTodoState.observe(this, Observer { state ->
            when (state) {
                is ArchiveTodoState.Success -> displayActionSnack(R.string.archive_success, R.string.undo, state.todoName) { viewModel.unarchiveTodo(state.todoId) }
                is ArchiveTodoState.UnknownError -> displayActionSnack(R.string.error_message, R.string.retry) { viewModel.archiveTodo(state.todoId) }
            }
        })
    }

    private fun observeUnarchiveTodoState() {
        viewModel.unarchiveTodoState.observe(this, Observer { state ->
            when (state) {
                is UnarchiveTodoState.UnknownError -> displayActionSnack(R.string.error_message, R.string.retry) { viewModel.unarchiveTodo(state.todoId) }
            }
        })
    }

    //endregion

    //region navigation

    private fun startEditActivity(todoId: Long) {
        startActivity(navigationHandler.buildEditTodoIntent(this, todoId))
    }

    override fun onBackPressed() {
        if (viewModel.isShowingAchivedTodos()) {
            bottomNavigation.selectedItemId = R.id.action_todo_list
        } else {
            super.onBackPressed()
        }
    }

}