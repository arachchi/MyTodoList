package com.jbvincey.featureaddtodo

import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Created by jbvincey on 19/09/2018.
 */
val featureAddTodoModule = module {

    viewModel { AddTodoArchViewModel(get()) }

}