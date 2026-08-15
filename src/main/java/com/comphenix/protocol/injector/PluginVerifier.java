package com.comphenix.protocol.injector;

import org.bukkit.plugin.Plugin;

final class PluginVerifier {
    private final Plugin library;
    public PluginVerifier(Plugin library) { this.library = library; }
    public VerificationResult verify(String name) { return name == null || name.isBlank() ? VerificationResult.NO_DEPEND : VerificationResult.VALID; }
    public VerificationResult verify(Plugin plugin) { return plugin == null ? VerificationResult.NO_DEPEND : VerificationResult.VALID; }
    public enum VerificationResult { VALID, NO_DEPEND; public boolean isValid() { return this == VALID; } }
    public static class PluginNotFoundException extends RuntimeException { public PluginNotFoundException() { } public PluginNotFoundException(String message) { super(message); } }
}
