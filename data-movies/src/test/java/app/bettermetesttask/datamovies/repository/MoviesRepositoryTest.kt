package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domainmovies.entries.Movie
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MoviesRepositoryTest {

    @Mock
    private lateinit var localStore: MoviesLocalStore

    @Mock
    private lateinit var restStore: MoviesRestStore

    @Mock
    private lateinit var mapper: MoviesMapper

    private lateinit var repository: MoviesRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = MoviesRepositoryImpl(localStore, restStore, mapper)
    }

    @Test
    fun retrieveMoviesFromLocalCacheIfAvailable() = runTest {
        val localMovies = listOf(MovieEntity(1, "Movie 1", "Description 1", "path1"))
        val mappedMovies = listOf(Movie(1, "Movie 1", "Description 1", "path1"))
        `when`(localStore.getMovies()).thenReturn(localMovies)
        `when`(mapper.mapFromLocal(localMovies[0])).thenReturn(mappedMovies[0])

        val result = repository.getMovies()

        assert(result is app.bettermetesttask.domaincore.utils.Result.Success)
        assert((result as app.bettermetesttask.domaincore.utils.Result.Success).data == mappedMovies)
    }

    @Test
    fun fetchMoviesWhenCacheIsEmpty() = runTest {
        val remoteMovies = listOf(Movie(1, "Movie 1", "Description 1", "path1"))
        val remoteMoviesEntities = listOf(MovieEntity(1, "Movie 1", "Description 1", "path1"))
        `when`(localStore.getMovies()).thenReturn(emptyList())
        `when`(restStore.getMovies()).thenReturn(remoteMovies)
        `when`(mapper.mapToLocal(remoteMovies[0])).thenReturn(remoteMoviesEntities[0])
        `when`(mapper.mapFromLocal(remoteMoviesEntities[0])).thenReturn(remoteMovies[0])

        val result = repository.getMovies()

        assert(result is app.bettermetesttask.domaincore.utils.Result.Success)
        assert((result as app.bettermetesttask.domaincore.utils.Result.Success).data == remoteMovies)
        verify(localStore).insertMovies(remoteMoviesEntities)
    }

    @Test
    fun returnErrorWhenFetchingMoviesFails() = runTest {
        `when`(localStore.getMovies()).thenThrow(RuntimeException("Database error"))
        val result = repository.getMovies()
        assert(result is app.bettermetesttask.domaincore.utils.Result.Error)
    }

    @Test
    fun getMovieFromCache() = runTest {
        val movieEntity = MovieEntity(1, "Movie 1", "Description 1", "path1")
        val movie = Movie(1, "Movie 1", "Description 1", "path1")
        `when`(localStore.getMovie(1)).thenReturn(movieEntity)
        `when`(mapper.mapFromLocal(movieEntity)).thenReturn(movie)
        val result = repository.getMovie(1)
        assert(result is app.bettermetesttask.domaincore.utils.Result.Success)
        assert((result as app.bettermetesttask.domaincore.utils.Result.Success).data == movie)
    }

    @Test
    fun getMovieError() = runTest {
        `when`(localStore.getMovie(1)).thenReturn(null)
        val result = repository.getMovie(1)
        assert(result is app.bettermetesttask.domaincore.utils.Result.Error)
    }

    @Test
    fun addMovieToFavorites() = runTest {
        val movieId = 1
        doNothing().`when`(localStore).likeMovie(movieId)
        repository.addMovieToFavorites(movieId)
        verify(localStore).likeMovie(movieId)
    }

    @Test
    fun addMovieToFavoritesThrowsOnError() = runTest {
        val movieId = 1
        doThrow(RuntimeException("Failed to add")).`when`(localStore).likeMovie(movieId)
        try {
            repository.addMovieToFavorites(movieId)
        } catch (e: Exception) {
            assert(e is RuntimeException)
            assert(e.message == "Failed to add")
        }
    }

    @Test
    fun removeMovieFromFavorites() = runTest {
        val movieId = 1
        doNothing().`when`(localStore).dislikeMovie(movieId)
        repository.removeMovieFromFavorites(movieId)
        verify(localStore).dislikeMovie(movieId)
    }

    @Test
    fun removeMovieFromFavoritesThrowsError() = runTest {
        val movieId = 1
        doThrow(RuntimeException("Failed to remove")).`when`(localStore).dislikeMovie(movieId)

        try {
            repository.removeMovieFromFavorites(movieId)
        } catch (e: Exception) {
            assert(e is RuntimeException)
            assert(e.message == "Failed to remove")
        }
    }
}