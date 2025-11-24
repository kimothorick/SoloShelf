#!/usr/bin/env bash
echo "
===============
|  Running detekt...
===============
"

./gradlew --no-daemon --stacktrace -PdisablePreDex detekt

detektStatus=$?

# return 1 exit code if running checks fails
[ $detektStatus -ne 0 ] && exit 1

######## KTLINT-GRADLE HOOK START ########

CHANGED_FILES="$(git --no-pager diff --name-status --no-color --cached | awk '$1 != "D" && $2 ~ /\.kts|\.kt/ { print $2}')"

if [ -z "$CHANGED_FILES" ]; then
    echo "No Kotlin staged files to lint."
else
    echo "Running ktlint on staged files:"
    echo "$CHANGED_FILES"

    echo "
===================
|  Formatting code with ktlintFormat...
===================
"
    ./gradlew ktlintFormat -PinternalKtlintGitFilter="$CHANGED_FILES"
    ktlintFormatStatus=$?
    [ $ktlintFormatStatus -ne 0 ] && exit 1

    echo "Adding formatted files back to git."
    echo "$CHANGED_FILES" | while read -r file; do
        if [ -f "$file" ]; then
            git add "$file"
        fi
    done

    echo "
===================
|  Running ktlintCheck...
===================
"
    ./gradlew --no-daemon --stacktrace -PdisablePreDex ktlintCheck -PinternalKtlintGitFilter="$CHANGED_FILES"

    checkStyleStatus=$?

    # return 1 exit code if running checks fails
    [ $checkStyleStatus -ne 0 ] && exit 1
fi

######## KTLINT-GRADLE HOOK END ########

exit 0
