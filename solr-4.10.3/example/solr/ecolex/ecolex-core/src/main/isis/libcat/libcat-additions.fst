### These lines need to be added to libcat.fst for ecolex-fts
120 0 (|DE=|v120^d.6/)
130 0 (|DM=|v130^d.6/)
120 0 if v120^d.4 < '1994' then 'PRD=B94' else if v120^d.4 < '1996' then 'PRD=B96' else if v120^d.4 < '1998' then 'PRD=B98' else if v120^d.4 < '2002' then 'PRD=B02' else if v120^d.4 < '2005' then 'PRD=B05' else 'PRD=A05' fi fi fi fi fi
