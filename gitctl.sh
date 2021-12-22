#!/bin/zsh

cd /Users/tiiaan/Projects/myssm
comment="$1"
day=$(date "+%-d")
time=$(date "+%H:%M:%S")
git add .
git commit -m "${comment}"
GIT_COMMITTER_DATE="December ${day} ${time} 2021 +0800" git commit --amend --date "December ${day} ${time} 2021 +0800"
git push -u origin master -f
#id=$(git rev-parse HEAD)
#memo="- [[${${id}: 0: 7}]](https://github.com/tiiaan/tidp/commit/${id})"
#echo "${memo} ${comment}" >> /Users/tiiaan/Projects/tidp/README.md
