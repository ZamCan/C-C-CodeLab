# Same-phone preview acceptance

Status: **not executed**. Installation is requested on the Android phone hosting the Termux session. The system package installer requires the owner's confirmation; a successful intent dispatch does not establish installation or launch.

Record device model, Android version, ABI, APK checksum, date, tester, and results. ADB currently reports no connected devices. Direct host property queries were denied by the environment, so device details must be obtained through authorized device access or Settings.

| Check | Expected behavior | Result |
| --- | --- | --- |
| Install APK | Android installer identifies ZamCan Academy and installs package com.zamcan.codelab | not run |
| Launch | No crash; two prominent destinations: Code Studio and Learning Academy | not run |
| Visual layout | Full background, readable translucent panels, visible button states, no clipped content | not run |
| Create project | Create ProjectOne and ProjectTwo; both remain available | not run |
| Source isolation | main.c in ProjectOne can differ from main.c in ProjectTwo | not run |
| Persistence | Edit text, wait for Saved, close/reopen app; source remains identical | not run |
| Rotation | Editing and rotation retain the latest saved source | not run |
| Indentation | Enter after an opening brace adds four spaces; hardware Tab inserts spaces | not run |
| Export | Save via Android document picker; verify exported source matches the editor | not run |
| Practice feedback | Wrong answer explains; correct answer records completion | not run |
| Learning persistence | Restart preserves course completion; C/C++ progress is separate | not run |
| Project guidance | Complete ten preview checks; five guides unlock independently of certificates | not run |
| Starter/reference | Open a copy without overwriting existing work; expected output labeled separately | not run |
| Offline use | Airplane mode preserves lessons, guides, editing, progress, and local export | not run |
| Accessibility | Large system text, TalkBack traversal, landscape, and keyboard do not hide actions | not run |
| Failure disclosure | Terminal reports no compiler; certificate path reports unavailable | not run |

Passing this checklist establishes a basic offline **preview** only. Native C/C++ execution requires the separate COMPILER_ENGINE.md matrix. Certification requires the CERTIFICATION_ENGINE.md threat, issuance, verification, and restoration tests. Neither is satisfied by installing this APK.
