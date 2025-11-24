sh
#!/usr/bin/env bash

# This pre-push hook runs critical checks before code is shared.
# Its main purpose is to run tests to ensure no regressions are introduced.

# --- Optional: Check which branch is being pushed to ---
# This is a safety net to prevent accidental pushes to main/master.
# It can be commented out if it doesn't fit your workflow.

# protected_branch='main'
# current_branch=$(git rev-parse --abbrev-ref HEAD)

# if [ "$current_branch" = "$protected_branch" ]; then
#   read -p "You are about to push to the '$protected_branch' branch. Are you sure? (y/n) " -n 1 -r
#   echo
#   if [[ ! $REPLY =~ ^[Yy]$ ]]; then
#     echo "Push to '$protected_branch' aborted."
#     exit 1
#   fi
# fi

# --- Run Unit Tests ---

echo "
======================================
|  Running unit tests before pushing...
======================================
"

# Executes all unit tests for the debug build variant.
# --no-daemon is used to ensure a clean run without using a background process.
./gradlew testDebugUnitTest

# Capture the exit code of the test command.
testStatus=$?

# If the exit code is not 0, the tests failed.
if [ $testStatus -ne 0 ]; then
  echo "
==================================
|  Unit tests failed. Push aborted.
|  Please fix the tests and try again.
==================================
"
  # Exit with a non-zero status to block the push.
  exit 1
fi

echo "
=====================================
|  All checks passed. Proceeding with push.
=====================================
"

# Exit with 0 to allow the push to continue.
exit 0
