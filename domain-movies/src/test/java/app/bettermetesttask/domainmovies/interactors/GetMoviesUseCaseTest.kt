package app.bettermetesttask.domainmovies.interactors

import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GetMoviesUseCaseTest {

    @Mock
    private lateinit var repository: MoviesRepository

    private lateinit var addMovieToFavoritesUseCase: AddMovieToFavoritesUseCase
    private lateinit var observeMoviesUseCase: ObserveMoviesUseCase
    private lateinit var removeMovieFromFavoritesUseCase: RemoveMovieFromFavoritesUseCase

    @BeforeEach
    fun setUp() {
        addMovieToFavoritesUseCase = AddMovieToFavoritesUseCase(repository)
        observeMoviesUseCase = ObserveMoviesUseCase(repository)
        removeMovieFromFavoritesUseCase = RemoveMovieFromFavoritesUseCase(repository)
    }

    @Test
    fun addMovieToFavorites() = runTest {
        val movieId = 1
        addMovieToFavoritesUseCase(movieId)
        verify(repository).addMovieToFavorites(movieId)
    }

    @Test
    fun observeMoviesWithLikedStatus() = runTest {
        val movies = listOf(
            Movie(id = 1, title = "Movie #1", description = "Description", posterPath = null),
            Movie(id = 2, title = "Movie #2", description = "Description", posterPath = null)
        )
        val likedMovieIds = listOf(1)

        `when`(repository.getMovies()).thenReturn(
            app.bettermetesttask.domaincore.utils.Result.Success(
                movies
            )
        )
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(likedMovieIds))

        val result = observeMoviesUseCase()

        result.collect { res ->
            if (res is app.bettermetesttask.domaincore.utils.Result.Success) {
                assertEquals(true, res.data.first().liked)
                assertEquals(false, res.data.last().liked)
            }
        }
    }

    @Test
    fun removeMovieFromFavorites() = runTest {
        val movieId = 1
        removeMovieFromFavoritesUseCase(movieId)
        verify(repository).removeMovieFromFavorites(movieId)
    }
}