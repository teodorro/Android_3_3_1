package ru.netology.nmedia.work

import androidx.work.DelegatingWorkerFactory
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DependencyWorkerFactory @Inject constructor(
    repository: PostRepository,
) : DelegatingWorkerFactory() {
    init {
        addFactory(RefreshPostsWorker.Factory(repository))
        addFactory(SavePostWorker.Factory(repository))
        addFactory(RemovePostWorker.Factory(repository))
    }
}