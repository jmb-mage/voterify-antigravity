import SwiftUI

struct LoginView: View {
    @EnvironmentObject var store: AppStore
    @State private var customDisplayName: String = ""
    @State private var loginAsAdmin: Bool = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 10) {
                        Image(systemName: "checkmark.shield.fill")
                            .font(.system(size: 60))
                            .foregroundStyle(.blue.gradient)

                        Text("Voterify")
                            .font(.largeTitle.bold())

                        Text("Anonymous, Blockchain-Verified Civic Elections")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.top, 28)

                    // Role & Display Name Configuration
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Account Setup")
                            .font(.headline)

                        TextField("Optional Display Name (or leave blank for OAuth default)", text: $customDisplayName)
                            .textFieldStyle(.roundedBorder)

                        Toggle(isOn: $loginAsAdmin) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Sign In with Admin Permissions")
                                    .font(.subheadline.weight(.medium))
                                Text("Grants Admin access to create & modify Elections, Candidates, Regions, Parties, and Permissions")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 14))

                    // OAuth Provider Buttons (Apple, Google, Facebook, Twitter)
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Sign In with OAuth Provider")
                            .font(.headline)

                        ForEach(OAuthProvider.allCases) { provider in
                            Button {
                                store.loginWithOAuth(
                                    provider: provider,
                                    customDisplayName: customDisplayName,
                                    asAdminPreset: loginAsAdmin
                                )
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: provider.systemIconName)
                                        .font(.title3)
                                        .frame(width: 28)

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Continue with \(provider.displayName)")
                                            .font(.body.weight(.semibold))
                                        Text("Client ID: \(store.oauthService.clientId(for: provider))")
                                            .font(.caption2)
                                            .opacity(0.8)
                                            .lineLimit(1)
                                    }

                                    Spacer()

                                    Image(systemName: "arrow.right.circle.fill")
                                }
                                .padding()
                                .frame(maxWidth: .infinity)
                                .foregroundStyle(.white)
                                .background(backgroundColor(for: provider))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                            }
                        }
                    }

                    // Privacy & Firestore Info Banner
                    VStack(alignment: .leading, spacing: 6) {
                        Label("Anonymous Voter GUID Protection", systemImage: "lock.shield")
                            .font(.footnote.weight(.semibold))
                        Text("When you sign in via OAuth, Voterify generates an anonymous VoterModel with a public GUID name and a private UserModel link. Your vote is recorded on the public blockchain using only your anonymous Voter GUID and vote selection.")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(.tertiarySystemBackground))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding()
            }
        }
    }

    private func backgroundColor(for provider: OAuthProvider) -> Color {
        switch provider {
        case .apple: return Color.black
        case .google: return Color.blue
        case .facebook: return Color.indigo
        case .twitter: return Color.cyan.opacity(0.85)
        }
    }
}
