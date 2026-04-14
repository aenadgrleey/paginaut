import Shared
import SwiftUI

@MainActor
final class DemoPagerStore: ObservableObject {
    let pager = DemoPagerFactory.shared.makePager()

    deinit {
        pager.close()
    }
}

struct ContentView: View {
    @StateObject private var store = DemoPagerStore()

    var body: some View {
        NavigationStack {
            PaginatedList(pager: store.pager, indicators: indicators) { item in
                VStack(alignment: .leading, spacing: 6) {
                    Text(item.title)
                        .font(.headline)
                    Text(item.subtitle)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(
                    RoundedRectangle(cornerRadius: 16)
                        .fill(Color(uiColor: .secondarySystemBackground))
                )
            }
            .padding(.horizontal, 16)
            .navigationTitle("Paginaut + Shared")
        }
    }

    private var indicators: PaginationIndicators {
        PaginationIndicators()
            .refreshIndicator {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
            .forwardLoadingIndicator {
                ProgressView()
                    .padding(.vertical, 16)
            }
            .endReachedIndicator {
                Text("All demo pages loaded")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .padding(.vertical, 20)
            }
    }
}
