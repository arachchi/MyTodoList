package com.jbvincey.todolist

import com.jbvincey.core.models.Todo
import com.jbvincey.core.transformers.Transformer
import com.jbvincey.ui.recycler.cells.checkablecell.CheckableCellCallback
import com.jbvincey.ui.recycler.cells.checkablecell.CheckableCellViewModel

/**
 * Created by jbvincey on 28/09/2018.
 */
class TodoToCheckableCellViewModelTransformer: Transformer<Todo, CheckableCellViewModel> {

    lateinit var checkableCellCallback: CheckableCellCallback

    override fun transform(source: Todo): CheckableCellViewModel {
        return CheckableCellViewModel(
                source.id,
                source.name,
                source.completed,
                source.creationDate,
                source.completionDate,
                source.archived,
                checkableCellCallback
        )
    }


}