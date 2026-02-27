@preconcurrency import PaginautCore
import SwiftUI

public struct PaginationIndicators {

    var refreshIndicator: AnyView
    var refreshErrorIndicator: (KotlinThrowable) -> AnyView
    var emptyIndicator: AnyView
    var forwardLoadingIndicator: AnyView
    var forwardErrorIndicator: (KotlinThrowable) -> AnyView
    var backwardLoadingIndicator: AnyView
    var backwardErrorIndicator: (KotlinThrowable) -> AnyView
    var endReachedIndicator: AnyView

    public init() {
        self.refreshIndicator = AnyView(EmptyView())
        self.refreshErrorIndicator = { _ in AnyView(EmptyView()) }
        self.emptyIndicator = AnyView(EmptyView())
        self.forwardLoadingIndicator = AnyView(EmptyView())
        self.forwardErrorIndicator = { _ in AnyView(EmptyView()) }
        self.backwardLoadingIndicator = AnyView(EmptyView())
        self.backwardErrorIndicator = { _ in AnyView(EmptyView()) }
        self.endReachedIndicator = AnyView(EmptyView())
    }

    public func refreshIndicator<V: View>(@ViewBuilder _ content: @escaping () -> V) -> Self {
        var copy = self
        copy.refreshIndicator = AnyView(content())
        return copy
    }

    public func refreshErrorIndicator<V: View>(
        @ViewBuilder _ content: @escaping (KotlinThrowable) -> V
    ) -> Self {
        var copy = self
        copy.refreshErrorIndicator = { AnyView(content($0)) }
        return copy
    }

    public func emptyIndicator<V: View>(@ViewBuilder _ content: @escaping () -> V) -> Self {
        var copy = self
        copy.emptyIndicator = AnyView(content())
        return copy
    }

    public func forwardLoadingIndicator<V: View>(@ViewBuilder _ content: @escaping () -> V) -> Self {
        var copy = self
        copy.forwardLoadingIndicator = AnyView(content())
        return copy
    }

    public func forwardErrorIndicator<V: View>(
        @ViewBuilder _ content: @escaping (KotlinThrowable) -> V
    ) -> Self {
        var copy = self
        copy.forwardErrorIndicator = { AnyView(content($0)) }
        return copy
    }

    public func backwardLoadingIndicator<V: View>(@ViewBuilder _ content: @escaping () -> V) -> Self {
        var copy = self
        copy.backwardLoadingIndicator = AnyView(content())
        return copy
    }

    public func backwardErrorIndicator<V: View>(
        @ViewBuilder _ content: @escaping (KotlinThrowable) -> V
    ) -> Self {
        var copy = self
        copy.backwardErrorIndicator = { AnyView(content($0)) }
        return copy
    }

    public func endReachedIndicator<V: View>(@ViewBuilder _ content: @escaping () -> V) -> Self {
        var copy = self
        copy.endReachedIndicator = AnyView(content())
        return copy
    }
}
