package app.bettermetesttask.movies.sections

import app.bettermetesttask.domainmovies.entries.Movie

sealed class SelectedMovieState {
    object Closed : SelectedMovieState()
    data class Open(val movie: Movie) : SelectedMovieState()
}