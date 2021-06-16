package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity.Companion.fromDto

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val wasSeen: Boolean = false,
    @Embedded
    var attachment: AttachmentEmbeddable?,
) {
    fun toDto() = Post(id, authorId, author, authorAvatar, content, published, likedByMe, likes, attachment?.toDto(),)

    // wasSeen всегда false
    companion object {
        fun fromDto(dto: Post, wasSeen: Boolean) =
            PostEntity(dto.id, dto.authorId, dto.author, dto.authorAvatar, dto.content, dto.published, dto.likedByMe, dto.likes, wasSeen, AttachmentEmbeddable.fromDto(dto.attachment))

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(wasSeen: Boolean): List<PostEntity> {
    var postEntities = mutableListOf<PostEntity>()
    for (post in this){
        postEntities.add(fromDto(post, wasSeen))
    }
    return postEntities
}

