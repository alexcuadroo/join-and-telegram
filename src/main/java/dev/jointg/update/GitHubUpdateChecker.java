package dev.jointg.update;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Checks for newer plugin versions from GitHub Releases.
 */
public final class GitHubUpdateChecker {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private GitHubUpdateChecker() {
    }

    /**
     * Checks GitHub for updates and logs the result to the console.
     *
     * @param plugin     the plugin instance
     * @param owner      the GitHub repository owner
     * @param repository the GitHub repository name
     */
    public static void checkForUpdates(JavaPlugin plugin, String owner, String repository) {
        if (plugin == null || owner == null || repository == null || owner.isBlank() || repository.isBlank()) {
            return;
        }

        String currentVersion = plugin.getPluginMeta().getVersion();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                String apiUrl = "https://api.github.com/repos/"
                        + encodePathSegment(owner)
                        + "/"
                        + encodePathSegment(repository)
                        + "/releases/latest";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/vnd.github+json")
                        .header("User-Agent", "JoinTelegram-UpdateChecker")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() >= 400) {
                    plugin.getComponentLogger().debug(
                            "Could not check updates from GitHub (HTTP " + response.statusCode() + ").");
                    return;
                }

                String body = response.body();
                String latestTag = extractJsonString(body, "tag_name");
                if (latestTag == null || latestTag.isBlank()) {
                    plugin.getComponentLogger().debug("Could not parse latest release tag from GitHub response.");
                    return;
                }

                String releaseUrl = extractJsonString(body, "html_url");
                if (isNewerVersion(currentVersion, latestTag)) {
                    String fallbackReleaseUrl = "https://github.com/" + owner + "/" + repository + "/releases";
                    String resolvedReleaseUrl = releaseUrl == null || releaseUrl.isBlank() ? fallbackReleaseUrl
                            : releaseUrl;
                    plugin.getComponentLogger().warn(
                            "A new version is available: current="
                                    + currentVersion
                                    + ", latest="
                                    + latestTag
                                    + ", url="
                                    + resolvedReleaseUrl);
                } else {
                    plugin.getComponentLogger().debug(
                            "No updates found. Current version (" + currentVersion + ") is up to date.");
                }
            } catch (IOException e) {
                plugin.getComponentLogger().debug("Could not check updates from GitHub due to network error.", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                plugin.getComponentLogger().debug("GitHub update check was interrupted.", e);
            } catch (RuntimeException e) {
                plugin.getComponentLogger().debug("Could not process GitHub update response.", e);
            }
        });
    }

    private static String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String extractJsonString(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).replace("\\\\/", "/");
    }

    private static boolean isNewerVersion(String currentVersion, String latestTag) {
        List<Integer> current = toVersionParts(currentVersion);
        List<Integer> latest = toVersionParts(latestTag);
        int maxParts = Math.max(current.size(), latest.size());

        for (int i = 0; i < maxParts; i++) {
            int currentPart = i < current.size() ? current.get(i) : 0;
            int latestPart = i < latest.size() ? latest.get(i) : 0;
            if (latestPart > currentPart) {
                return true;
            }
            if (latestPart < currentPart) {
                return false;
            }
        }
        return false;
    }

    private static List<Integer> toVersionParts(String version) {
        String normalized = normalizeVersion(version);
        Matcher matcher = NUMBER_PATTERN.matcher(normalized);
        List<Integer> parts = new ArrayList<>();
        while (matcher.find()) {
            parts.add(Integer.parseInt(matcher.group()));
        }
        if (parts.isEmpty()) {
            parts.add(0);
        }
        return parts;
    }

    private static String normalizeVersion(String version) {
        if (version == null) {
            return "";
        }
        String normalized = version.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }

        while (!normalized.isEmpty() && (normalized.charAt(0) == 'v' || normalized.charAt(0) == 'V')) {
            normalized = normalized.substring(1).trim();
        }

        while (!normalized.isEmpty() && !Character.isDigit(normalized.charAt(0))) {
            normalized = normalized.substring(1).trim();
        }

        return normalized;
    }
}
