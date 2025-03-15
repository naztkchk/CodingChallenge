package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.coroutines.AppDispatchers
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val restStore: MoviesRestStore,
    private val mapper: MoviesMapper
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> {
        return withContext(AppDispatchers.io()) {
            try {
                val localMovies = localStore.getMovies()
                if (localMovies.isNotEmpty()) {
                    Result.Success(localMovies.map { mapper.mapFromLocal(it) })
                } else {
                    val remoteMovies = restStore.getMovies()
                    localStore.insertMovies(remoteMovies.map { mapper.mapToLocal(it) })
                    Result.Success(remoteMovies)
                }
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    override suspend fun getMovie(id: Int): Result<Movie> {
        return withContext(AppDispatchers.io()) {
            Result.of { mapper.mapFromLocal(localStore.getMovie(id)) }
        }
    }

    override fun observeLikedMovieIds(): Flow<List<Int>> {
        return localStore.observeLikedMoviesIds()
    }

    override suspend fun addMovieToFavorites(movieId: Int) {
        withContext(AppDispatchers.io()) {
            localStore.likeMovie(movieId)
        }
    }

    override suspend fun removeMovieFromFavorites(movieId: Int) {
        withContext(AppDispatchers.io()) {
            localStore.dislikeMovie(movieId)
        }
    }
}