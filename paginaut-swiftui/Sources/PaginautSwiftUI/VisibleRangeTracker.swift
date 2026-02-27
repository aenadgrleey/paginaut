@preconcurrency import PaginautCore
import SwiftUI

@MainActor
final class VisibleRangeTracker: ObservableObject {

    private var visibleIndices: Set<Int> = []
    private let pager: any Pager

    init(pager: some Pager) {
        self.pager = pager
    }

    func onItemAppear(index: Int) {
        visibleIndices.insert(index)
        reportRange()
    }

    func onItemDisappear(index: Int) {
        visibleIndices.remove(index)
    }

    private func reportRange() {
        guard let first = visibleIndices.min(),
              let last = visibleIndices.max() else { return }
        pager.onVisibleRangeChanged(
            range: VisibleRange(firstVisible: Int32(first), lastVisible: Int32(last))
        )
    }
}
