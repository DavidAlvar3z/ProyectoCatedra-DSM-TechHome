package com.techhome.models

import androidx.annotation.DrawableRes

data class Category(
    val name: String,
    val id: String,
    @DrawableRes val icon: Int,
    @DrawableRes val gradient: Int
)