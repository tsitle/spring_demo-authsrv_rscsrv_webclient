#!/bin/bash

#
# by TS, May 2019, Aug 2022
#

# ----------------------------------------------------------

# update Docker Image from remote repository? [true|false]
LCFG_UPDATE_REMOTE_IMAGE=false

# ----------------------------------------------------------

# @param string $1 Path
# @param int $2 Recursion level
#
# @return string Absolute path
function realpath_osx() {
	local TMP_RP_OSX_RES=
	[[ $1 = /* ]] && TMP_RP_OSX_RES="$1" || TMP_RP_OSX_RES="$PWD/${1#./}"

	if [ -h "$TMP_RP_OSX_RES" ]; then
		TMP_RP_OSX_RES="$(readlink "$TMP_RP_OSX_RES")"
		# possible infinite loop...
		local TMP_RP_OSX_RECLEV=$2
		[ -z "$TMP_RP_OSX_RECLEV" ] && TMP_RP_OSX_RECLEV=0
		TMP_RP_OSX_RECLEV=$(( TMP_RP_OSX_RECLEV + 1 ))
		if [ $TMP_RP_OSX_RECLEV -gt 20 ]; then
			# too much recursion
			TMP_RP_OSX_RES="--error--"
		else
			TMP_RP_OSX_RES="$(realpath_osx "$TMP_RP_OSX_RES" $TMP_RP_OSX_RECLEV)"
		fi
	fi
	echo "$TMP_RP_OSX_RES"
}

# @param string $1 Path
#
# @return string Absolute path
function realpath_poly() {
	case "$OSTYPE" in
		linux*) realpath "$1" ;;
		darwin*) realpath_osx "$1" ;;
		*) echo "$VAR_MYNAME: Error: Unknown OSTYPE '$OSTYPE'" >/dev/stderr; echo -n "$1" ;;
	esac
}

VAR_MYNAME="$(basename "$0")"
VAR_MYDIR="$(realpath_poly "$0")"
VAR_MYDIR="$(dirname "$VAR_MYDIR")"

# ----------------------------------------------------------

# Outputs CPU architecture string
#
# @param string $1 debian_rootfs|debian_dist
#
# @return int EXITCODE
function _getCpuArch() {
	case "$(uname -m)" in
		x86_64*)
			echo -n "amd64"
			;;
		i686*)
			echo -n "i386"
			;;
		aarch64*)
			echo -n "arm64"
			;;
		armv7*)
			echo -n "armhf"
			;;
		*)
			echo "$VAR_MYNAME: Error: Unknown CPU architecture '$(uname -m)'" >/dev/stderr
			return 1
			;;
	esac
	return 0
}

_getCpuArch debian_dist >/dev/null || exit 1


LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

# ----------------------------------------------------------

VAR_DC_SERVICE_DEF="spring-demo-auth-srv"

function printUsageAndExit() {
	echo "Usage: $VAR_MYNAME <COMMAND> ..." >/dev/stderr
	echo "Examples: $VAR_MYNAME up" >/dev/stderr
	echo "          $VAR_MYNAME start" >/dev/stderr
	echo "          $VAR_MYNAME start $VAR_DC_SERVICE_DEF" >/dev/stderr
	echo "          $VAR_MYNAME stop" >/dev/stderr
	echo "          $VAR_MYNAME stop $VAR_DC_SERVICE_DEF" >/dev/stderr
	echo "          $VAR_MYNAME restart $VAR_DC_SERVICE_DEF" >/dev/stderr
	echo "          $VAR_MYNAME ps" >/dev/stderr
	echo "          $VAR_MYNAME rm" >/dev/stderr
	echo "          $VAR_MYNAME down" >/dev/stderr
	echo "          $VAR_MYNAME logs -f" >/dev/stderr
	echo "          $VAR_MYNAME logs -f $VAR_DC_SERVICE_DEF" >/dev/stderr
	echo "          $VAR_MYNAME exec $VAR_DC_SERVICE_DEF bash" >/dev/stderr
	echo "            or simply" >/dev/stderr
	echo "          $VAR_MYNAME bash $VAR_DC_SERVICE_DEF" >/dev/stderr
	exit 1
}

cd "$VAR_MYDIR" || exit 1

if [ $# -lt 1 ]; then
	printUsageAndExit
fi

OPT_CMD="$1"
shift

# ----------------------------------------------------------

function _createDockComp() {
	local TMP_UID="$(id -u)"
	local TMP_GID="$(id -g)"

	[ $TMP_UID -lt 500 ] && TMP_UID=1000
	[ $TMP_GID -lt 101 ] && TMP_GID=1000
	#
	cp "docker-compose-SAMPLE.yaml" "$VAR_DCY_INP" || return 1
	#
	sed \
			-i.bck \
			-e "s/<CPU_ARCH>/$LVAR_DEBIAN_DIST/g" \
			-e "s/<YOUR_UID>/$TMP_UID/g" \
			-e "s/<YOUR_GID>/$TMP_GID/g" \
			"$VAR_DCY_INP" || return 1
	rm "${VAR_DCY_INP}.bck" || return 1
	return 0
}

VAR_DCY_INP="docker-compose-apps.yaml"

VAR_PROJNAME="springdemo-jdk"

[ ! -f "$VAR_DCY_INP" -a -f "docker-compose-SAMPLE.yaml" ] && {
	_createDockComp || exit 1
}

[ ! -f "$VAR_DCY_INP" ] && {
	echo "$VAR_MYNAME: Error: File '$VAR_DCY_INP' not found. Aborting." >/dev/stderr
	exit 1
}

# ----------------------------------------------------------

function _syncApps() {
		echo -e "\n-------------------------------"
		echo      "- Syncing Auth Server Sources -"
		echo      "-------------------------------"
		rsync -va --delete --exclude=build --exclude=.DS_Store \
				../gradle \
				../gradlew \
				../build.gradle.kts \
				../settings.gradle.kts \
				../mod_auth_server \
				../mod_z_common \
				mp_authsrv_app/ || exit 1

		echo -e "\n-----------------------------------"
		echo      "- Syncing Resource Server Sources -"
		echo      "-----------------------------------"
		rsync -va --delete --exclude=build --exclude=.DS_Store \
				../gradle \
				../gradlew \
				../build.gradle.kts \
				../settings.gradle.kts \
				../mod_resource_server \
				../mod_z_common \
				mp_rscsrv_app/ || exit 1

		echo -e "\n------------------------------"
		echo      "- Syncing Web Client Sources -"
		echo      "------------------------------"
		rsync -va --delete --exclude=build --exclude=.DS_Store \
				../gradle \
				../gradlew \
				../build.gradle.kts \
				../settings.gradle.kts \
				../mod_webclient \
				../mod_z_common \
				mp_webclient_app/ || exit 1

		#
		local TMP_DOCKER_HOST="host.docker.internal"
		sed -i '' \
				-e 's;spring.data.mongodb.host=.*;spring.data.mongodb.host='$TMP_DOCKER_HOST';g' \
				mp_authsrv_app/mod_auth_server/src/main/resources/application.properties
		sed -i '' \
				-e 's;spring.data.mongodb.host=.*;spring.data.mongodb.host='$TMP_DOCKER_HOST';g' \
				mp_rscsrv_app/mod_resource_server/src/main/resources/application.properties
		sed -i '' \
				-e 's;custom-app.resource-server.url=.*;custom-app.resource-server.url=http://'$TMP_DOCKER_HOST':8090;g' \
				mp_webclient_app/mod_webclient/src/main/resources/application.properties

		#
		sed -i '' \
				-e 's,include("mod_resource_server"),,g' \
				-e 's,include("mod_webclient"),,g' \
				mp_authsrv_app/settings.gradle.kts
		sed -i '' \
				-e 's,include("mod_auth_server"),,g' \
				-e 's,include("mod_webclient"),,g' \
				mp_rscsrv_app/settings.gradle.kts
		sed -i '' \
				-e 's,include("mod_auth_server"),,g' \
				-e 's,include("mod_resource_server"),,g' \
				mp_webclient_app/settings.gradle.kts
}

# ----------------------------------------------------------

function _updateRemoteImages() {
	local TMP_IMGLIST="$(grep "image:" "$VAR_DCY_INP" | grep -v -E ".*#.*image:" | grep "/" | awk '{print $2}' | tr -d \"\')"
	[ -z "$TMP_IMGLIST" ] && return 0
	local TMP_IMGENTRY
	for TMP_IMGENTRY in $TMP_IMGLIST; do
		echo "$VAR_MYNAME: Updating image '${TMP_IMGENTRY}'..."
		docker pull ${TMP_IMGENTRY} || return 1
		echo
	done
	return 0
}

TMP_ADDITIONAL_OPT=
if [ "$OPT_CMD" = "up" ]; then
	# prevent Docker Compose from running in foreground
	TMP_ADDITIONAL_OPT="--no-start"
	#
	if [ "$LCFG_UPDATE_REMOTE_IMAGE" = "true" ]; then
		_updateRemoteImages || exit 1
	fi
fi

if [ "$OPT_CMD" = "bash" ]; then
	OPT_CMD="sh"
	OPT_SERVICE="$1"
	shift
	[ -z "$OPT_SERVICE" ] && OPT_SERVICE="$VAR_DC_SERVICE_DEF"
	docker-compose -p "$VAR_PROJNAME" -f "$VAR_DCY_INP" exec "$OPT_SERVICE" "$OPT_CMD" $@ || exit 1
else
	docker-compose -p "$VAR_PROJNAME" -f "$VAR_DCY_INP" "$OPT_CMD" $TMP_ADDITIONAL_OPT $@ || exit 1
fi

if [ "$OPT_CMD" = "up" ]; then
	# sync files into containers
	_syncApps

	# now start services in background
	docker-compose -p "$VAR_PROJNAME" -f "$VAR_DCY_INP" start || exit 1

	# now start the individual apps
	#command: /bin/sh -c ./gradlew :mod_auth_server:bootJar && java -jar mod_auth_server/build/libs/mod_auth_server-0.0.1-SNAPSHOT.jar
fi

exit 0
