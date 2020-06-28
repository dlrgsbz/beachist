package de.tjarksaul.wachmanager.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Field(
    val id: String, // todo: id
    val name: String,
    val parent: String? = null,
    val required: Int? = null,
    val note: String? = null,
    var entry: Entry? = null
) {
//    var id = Random.nextBytes(24).toString()

//    constructor(name: String, parent: String?, required: Int?, note: String?, state: Boolean?, id: String): this(name, parent, required, note, state) {
//        this.id = id
//    }
}