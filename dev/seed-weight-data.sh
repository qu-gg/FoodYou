#!/usr/bin/env bash
# Seed the weight tracker with ~90 days of test data via adb.
# Usage: ./dev/seed-weight-data.sh [package_suffix]
#   package_suffix: empty for release, ".preview" for preview builds (default: empty)
#
# Requires: adb in PATH (or ANDROID_HOME set)

set -euo pipefail

SUFFIX="${1:-}"
PACKAGE="com.maksimowiczm.foodyou${SUFFIX}"
DB_NAME="open_source_database.db"
DB_PATH="/data/data/${PACKAGE}/databases/${DB_NAME}"

ADB="${ANDROID_HOME:+$ANDROID_HOME/platform-tools/}adb"

echo "==> Seeding weight data for $PACKAGE"
echo "    DB path: $DB_PATH"

# Generate ~90 days of sample weight entries.
# Simulates a weight-loss trend from ~85 kg to ~78 kg with some noise.
TODAY_EPOCH_DAY=$(python3 -c "from datetime import date; print(date.today().toordinal() - 719163)")

SQL="BEGIN TRANSACTION;"
for i in $(seq 90 -1 0); do
    EPOCH_DAY=$((TODAY_EPOCH_DAY - i))
    # Linear trend from 85 → 78 kg, plus some random-ish noise
    BASE_KG=$(python3 -c "
import math, random
random.seed($i)
trend = 85.0 - (90 - $i) * (7.0 / 90)
noise = random.gauss(0, 0.4)
print(round(trend + noise, 1))
")
    SQL="${SQL} INSERT OR REPLACE INTO WeightEntry (dateEpochDay, weightKg) VALUES ($EPOCH_DAY, $BASE_KG);"
done
SQL="${SQL} COMMIT;"

# Execute via adb
echo "==> Inserting 91 weight entries..."
$ADB shell "run-as $PACKAGE sqlite3 $DB_PATH \"$SQL\"" 2>/dev/null \
    || $ADB shell "su -c \"sqlite3 $DB_PATH \\\"$SQL\\\"\"" 2>/dev/null \
    || {
        echo "Direct DB access failed. Trying via am broadcast..."
        echo "Falling back to writing SQL to a temp file..."
        TMPFILE=$(mktemp /tmp/seed_weight_XXXXXX.sql)
        echo "$SQL" > "$TMPFILE"
        $ADB push "$TMPFILE" /data/local/tmp/seed_weight.sql
        $ADB shell "run-as $PACKAGE sqlite3 $DB_PATH < /data/local/tmp/seed_weight.sql"
        rm -f "$TMPFILE"
    }

echo "==> Done! Restart the app to see the chart."
echo "    $ADB shell am force-stop $PACKAGE"
echo "    $ADB shell monkey -p $PACKAGE -c android.intent.category.LAUNCHER 1"
