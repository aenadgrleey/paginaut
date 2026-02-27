@preconcurrency import PaginautCore

enum FullScreenPaginationState {
    case loading
    case error(KotlinThrowable)
    case empty
}

func fullScreenState(_ state: PaginationState<AnyObject>) -> FullScreenPaginationState? {
    if state.refresh is LoadStatusLoading && state.items.isEmpty {
        return .loading
    }
    if let error = state.refresh as? LoadStatusError, state.items.isEmpty {
        return .error(error.cause)
    }
    if state.items.isEmpty
        && state.refresh is LoadStatusIdle
        && state.forward is LoadStatusEndReached
    {
        return .empty
    }
    return nil
}
