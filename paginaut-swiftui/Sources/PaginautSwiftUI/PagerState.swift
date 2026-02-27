@preconcurrency import PaginautCore
import SwiftUI

@MainActor
public final class PagerState<Item: AnyObject>: ObservableObject {

    @Published public private(set) var paginationState: PaginationState<Item>

    private let pager: any Pager
    private var observationTask: Task<Void, Never>?

    public init(pager: some Pager) {
        self.pager = pager
        self.paginationState = pager.state.value as! PaginationState<Item>
        startObserving()
    }

    private func startObserving() {
        observationTask = Task { [weak self] in
            guard let pager = self?.pager else { return }
            for await state in pager.state {
                guard !Task.isCancelled else { break }
                self?.paginationState = state as! PaginationState<Item>
            }
        }
    }

    deinit {
        observationTask?.cancel()
    }
}
