@preconcurrency import PaginautCore
import SwiftUI

public struct PaginatedHGrid<Item: AnyObject, ItemContent: View>: View {

    @StateObject private var pagerState: PagerState<Item>
    @StateObject private var tracker: VisibleRangeTracker

    private let rows: [GridItem]
    private let indicators: PaginationIndicators
    private let spacing: CGFloat?
    private let contentInsets: EdgeInsets
    private let showsIndicators: Bool
    private let itemContent: (Item) -> ItemContent

    public init(
        pager: some Pager,
        rows: [GridItem],
        indicators: PaginationIndicators = PaginationIndicators(),
        spacing: CGFloat? = nil,
        contentInsets: EdgeInsets = .init(),
        showsIndicators: Bool = true,
        @ViewBuilder itemContent: @escaping (Item) -> ItemContent
    ) {
        self.rows = rows
        self.indicators = indicators
        self.spacing = spacing
        self.contentInsets = contentInsets
        self.showsIndicators = showsIndicators
        self.itemContent = itemContent
        _pagerState = StateObject(wrappedValue: PagerState(pager: pager))
        _tracker = StateObject(wrappedValue: VisibleRangeTracker(pager: pager))
    }

    public var body: some View {
        let state = pagerState.paginationState

        if let fullScreen = fullScreenState(state as! PaginationState<AnyObject>) {
            switch fullScreen {
            case .loading:
                indicators.refreshIndicator
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .error(let cause):
                indicators.refreshErrorIndicator(cause)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            case .empty:
                indicators.emptyIndicator
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        } else {
            ScrollView(.horizontal, showsIndicators: showsIndicators) {
                LazyHStack(spacing: 0) {
                    if state.backward is LoadStatusLoading {
                        indicators.backwardLoadingIndicator
                    }

                    if let error = state.backward as? LoadStatusError {
                        indicators.backwardErrorIndicator(error.cause)
                    }

                    LazyHGrid(rows: rows, spacing: spacing) {
                        ForEach(Array(state.items.enumerated()), id: \.offset) { index, item in
                            itemContent(item)
                                .onAppear { tracker.onItemAppear(index: index) }
                                .onDisappear { tracker.onItemDisappear(index: index) }
                        }
                    }

                    if state.forward is LoadStatusLoading {
                        indicators.forwardLoadingIndicator
                    }

                    if let error = state.forward as? LoadStatusError {
                        indicators.forwardErrorIndicator(error.cause)
                    }

                    if state.forward is LoadStatusEndReached && !state.items.isEmpty {
                        indicators.endReachedIndicator
                    }
                }
                .padding(contentInsets)
            }
        }
    }
}
