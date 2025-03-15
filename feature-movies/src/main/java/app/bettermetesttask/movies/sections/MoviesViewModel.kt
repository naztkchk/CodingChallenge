package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.coroutines.AppDispatchers
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
    private val adapter: MoviesAdapter
) : ViewModel() {

    private val _moviesStateFlow: MutableStateFlow<MoviesState> =
        MutableStateFlow(MoviesState.Initial)

    val moviesStateFlow: StateFlow<MoviesState>
        get() = _moviesStateFlow.asStateFlow()

    private val _selectedMovieStateFlow =
        MutableStateFlow<SelectedMovieState>(SelectedMovieState.Closed)
    val selectedMovieStateFlow: StateFlow<SelectedMovieState> =
        _selectedMovieStateFlow.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch(AppDispatchers.main()) {
            _moviesStateFlow.value = MoviesState.Loading
            try {
                observeMoviesUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _moviesStateFlow.emit(MoviesState.Loaded(result.data))
                            adapter.submitList(result.data)
                        }

                        is Result.Error -> {
                            _moviesStateFlow.emit(MoviesState.Error(result.error))
                        }
                    }
                }
            } catch (e: Exception) {
                _moviesStateFlow.value = MoviesState.Error(e)
            }
        }
    }

    fun likeMovie(movie: Movie) {
        viewModelScope.launch(AppDispatchers.main()) {
            try {
                withContext(AppDispatchers.io()) {
                    if (movie.liked) {
                        dislikeMovieUseCase(movie.id)
                    } else {
                        likeMovieUseCase(movie.id)
                    }
                }
            } catch (e: Exception) {
                _moviesStateFlow.value = MoviesState.Error(e)
            }
        }
    }

    fun openMovieDetails(movie: Movie) {
        viewModelScope.launch {
            _selectedMovieStateFlow.emit(SelectedMovieState.Open(movie))
        }
    }

    fun closeMovieDetails() {
        viewModelScope.launch {
            _selectedMovieStateFlow.emit(SelectedMovieState.Closed)
        }
    }
}