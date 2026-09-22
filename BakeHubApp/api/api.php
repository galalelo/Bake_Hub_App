<?php
// BakeHub REST API for XAMPP. Copy api/ to htdocs/bakehub-api/.
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

const DB_HOST = '127.0.0.1';
const DB_NAME = 'bakehub';
const DB_USER = 'root';
const DB_PASS = '';

function respond($payload, int $status = 200): void { http_response_code($status); echo json_encode($payload, JSON_UNESCAPED_UNICODE); exit; }
function body(): array { $raw = file_get_contents('php://input'); $data = json_decode($raw ?: '{}', true); return is_array($data) ? $data : []; }
function db(): PDO {
  static $pdo;
  if (!$pdo) $pdo = new PDO('mysql:host='.DB_HOST.';dbname='.DB_NAME.';charset=utf8mb4', DB_USER, DB_PASS, [PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE=>PDO::FETCH_ASSOC]);
  return $pdo;
}
function userId(): int { return 1; } // Demo session; replace with JWT validation for production.
function recipe(PDO $pdo, string $id): ?array {
  $s = $pdo->prepare('SELECT r.*, COALESCE(u.display_name, r.author_name) AS author_name FROM recipes r LEFT JOIN users u ON u.id=r.created_by WHERE r.id=?'); $s->execute([$id]); $r=$s->fetch(); if (!$r) return null;
  $s=$pdo->prepare('SELECT name,quantity,unit FROM ingredients WHERE recipe_id=? ORDER BY sort_order,id'); $s->execute([$id]); $r['ingredients']=$s->fetchAll();
  $s=$pdo->prepare('SELECT step_number,title,instruction,timer_seconds FROM recipe_steps WHERE recipe_id=? ORDER BY step_number'); $s->execute([$id]); $r['steps']=$s->fetchAll();
  $r['difficulty']=strtoupper($r['difficulty']); $r['nutrition']=['calories'=>(int)$r['calories'],'proteinG'=>(float)$r['protein_g'],'carbsG'=>(float)$r['carbs_g'],'fatG'=>(float)$r['fat_g']];
  foreach (['prep_time_minutes','cook_time_minutes','servings','rating_count'] as $k) $r[$k]=(int)$r[$k]; $r['avg_rating']=(float)$r['avg_rating'];
  return $r;
}
try {
  $pdo=db(); $method=$_SERVER['REQUEST_METHOD']; $path=parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH); $base=strpos($path,'api.php'); $tail=$base===false?'':substr($path,$base+7); $parts=array_values(array_filter(explode('/', trim($tail,'/'))));
  if ($method==='GET' && ($parts[0]??'')==='health') respond(['ok'=>true,'service'=>'BakeHub API','database'=>'mysql']);
  if ($method==='POST' && ($parts[0]??'')==='auth' && ($parts[1]??'')==='login') {
    $b=body(); $s=$pdo->prepare('SELECT id,display_name,email,skill_level,level,xp,streak_count FROM users WHERE email=? AND password_hash=SHA2(?,256)'); $s->execute([$b['email']??'', $b['password']??'']); $u=$s->fetch(); if (!$u) respond(['error'=>'Invalid email or password'],401); respond(['token'=>'demo-'.$u['id'],'user'=>$u]);
  }
  if ($method==='GET' && ($parts[0]??'')==='recipes') {
    if (isset($parts[1])) { $r=recipe($pdo,$parts[1]); if (!$r) respond(['error'=>'Recipe not found'],404); respond($r); }
    $q=trim($_GET['search']??''); $s=$pdo->prepare('SELECT id FROM recipes WHERE title LIKE ? OR category LIKE ? ORDER BY created_at DESC'); $like='%'.$q.'%'; $s->execute([$like,$like]); $out=[]; foreach($s as $row) $out[]=recipe($pdo,$row['id']); respond(['recipes'=>$out]);
  }
  if ($method==='GET' && ($parts[0]??'')==='me' && ($parts[1]??'')==='recipe-box') { $s=$pdo->prepare('SELECT recipe_id FROM saved_recipes WHERE user_id=?'); $s->execute([userId()]); $out=[]; foreach($s as $row) $out[]=recipe($pdo,$row['recipe_id']); respond(['recipes'=>$out]); }
  if ($method==='POST' && ($parts[0]??'')==='recipes' && ($parts[2]??'')==='save') { $s=$pdo->prepare('INSERT IGNORE INTO saved_recipes(user_id,recipe_id) VALUES(?,?)'); $s->execute([userId(),$parts[1]??'']); respond(['saved'=>true]); }
  if ($method==='POST' && ($parts[0]??'')==='recipes' && !isset($parts[1])) {
    $b=body(); if (trim($b['title']??'')==='') respond(['error'=>'title is required'],422); $id='user-'.bin2hex(random_bytes(4)); $pdo->beginTransaction();
    $s=$pdo->prepare('INSERT INTO recipes(id,title,author_name,source,category,difficulty,prep_time_minutes,cook_time_minutes,servings,created_by) VALUES(?,?,?,?,?,?,?,?,?,?)'); $s->execute([$id,$b['title'],'Alex Baker','User',$b['category']??'Other',$b['difficulty']??'EASY',(int)($b['prepTimeMinutes']??0),(int)($b['cookTimeMinutes']??0),(int)($b['servings']??1),userId()]);
    $s=$pdo->prepare('INSERT INTO ingredients(recipe_id,name,quantity,unit,sort_order) VALUES(?,?,?,?,?)'); foreach(($b['ingredients']??[]) as $i=>$x) $s->execute([$id,$x['name']??$x['text']??'Ingredient',$x['quantity']??'1',$x['unit']??'', $i+1]);
    $s=$pdo->prepare('INSERT INTO recipe_steps(recipe_id,step_number,title,instruction,timer_seconds) VALUES(?,?,?,?,?)'); foreach(($b['steps']??[]) as $i=>$x) $s->execute([$id,$i+1,'Step '.($i+1),is_array($x)?($x['instruction']??''): $x,null]); $pdo->commit(); respond(recipe($pdo,$id),201);
  }
  respond(['error'=>'Not found'],404);
} catch (Throwable $e) { if (isset($pdo) && $pdo->inTransaction()) $pdo->rollBack(); respond(['error'=>'Server error','detail'=>$e->getMessage()],500); }
