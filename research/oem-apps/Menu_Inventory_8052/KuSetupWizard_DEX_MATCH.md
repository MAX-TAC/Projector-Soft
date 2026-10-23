# KuSetupWizard / KuSettings DEX SHA256 comparison (8052)

Method: compare full original DEX file bytes from each APK, not only class name strings.

KuSettings DEX: 15, KuSetupWizard DEX: 15

Identical DEX files: 14 / 15

DEX filenames present in only one package: []

- classes.dex: identical
- classes10.dex: identical
- classes11.dex: identical
- classes12.dex: DIFFERENT
- classes13.dex: identical
- classes14.dex: identical
- classes15.dex: identical
- classes2.dex: identical
- classes3.dex: identical
- classes4.dex: identical
- classes5.dex: identical
- classes6.dex: identical
- classes7.dex: identical
- classes8.dex: identical
- classes9.dex: identical

KuSetupWizard is a distinct installed package com.android.provision with its own DEX, and only its own manifest components are activated.
DEX equality alone does not demonstrate runtime independence from implicit intents, settings provider, files, system services, or external package names.
Do not disable KuSetupWizard.
