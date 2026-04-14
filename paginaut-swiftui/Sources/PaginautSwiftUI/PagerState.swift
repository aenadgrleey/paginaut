@preconcurrency import PaginautCore
import SwiftUI

@MainActor
public final class PagerState<Key: AnyObject, Item: AnyObject>: ObservableObject {

    @Published public private(set) var paginationState: PaginationState<Item>

    private let stateFlow: SkieSwiftStateFlow<PaginationState<Item>>
    private var observationTask: Task<Void, Never>?

    public init(pager: BidirPager<Key, Item>) {
        let stateFlow = pager.state
        self.stateFlow = stateFlow
        self.paginationState = stateFlow.value
        startObserving()
    }

    private func startObserving() {
        observationTask = Task { [weak self] in
            guard let stateFlow = self?.stateFlow else { return }
            for await state in stateFlow {
                guard !Task.isCancelled else { break }
                self?.paginationState = state
            }
        }
    }

    deinit {
        observationTask?.cancel()
    }
}
