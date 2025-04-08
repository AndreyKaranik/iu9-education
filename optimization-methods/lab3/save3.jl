function simplex_method() 
    x1 = [0,0] 
    x2 = [(sqrt(3)+1)/(2*sqrt(2)), (sqrt(3)-1)/(2*sqrt(2))] 
    x3 = [(sqrt(3)-1)/(2*sqrt(2)), (sqrt(3)+1)/(2*sqrt(2))] 
    points = [x1,x2,x3] 
    push!(xs, x1) 
    push!(xs, x2) 
    push!(xs, x3) 
    while true 
        if(norm(points[1]- points[2])) < 0.001 
            return points[1] 
        end 
 
        if(f(points[2]) >= f(points[1]) && f(points[2]) >= f(points[3])) 
            temp = points[1] 
            points[1] = points[2] 
            points[2] = temp 
        elseif (f(points[3]) >= f(points[1]) && f(points[3]) >= f(points[2])) 
            temp = points[1] 
            points[1] = points[3] 
            points[3] = temp 
        end 
 
        x4 = points[2] + points[3] - points[1] 
        if(f(x4) >= f(points[2]) && f(x4) >= f(points[3])) 
            points[1] = x4 
            points[2] = x4+(points[2] - x4)/2 
            points[3] = x4+(points[3] - x4)/2 
            push!(xs, x4) 
            push!(xs, points[2])
            push!(xs, points[3]) 
        else 
            points[1] = x4 
            push!(xs, x4) 
            push!(xs, points[2]) 
            push!(xs, points[3]) 
        end 
    end 
end


function nelder_meed() 
    x1 = [0.0,0.0] 
    x2 = [(sqrt(3)+1)/(2*sqrt(2)), (sqrt(3)-1)/(2*sqrt(2))] 
    x3 = [(sqrt(3)-1)/(2*sqrt(2)), (sqrt(3)+1)/(2*sqrt(2))] 
    points = [x1,x2,x3] 
    xs = [] 
    center = [0.0,0.0] 
    beta = 2.0 
    push!(xs, x1) 
    push!(xs, x2) 
    push!(xs, x3) 
    while true 
        points = sort(points, by=x -> f(x), rev=true) 
        push!(xs, points[3]) 
        push!(xs, points[1]) 
        push!(xs, points[2]) 
        push!(xs, points[3]) 
        center = (points[2]+points[3])/2.0 
 
        if (sqrt(((f(points[1]) - f(center))^2 +((f(points[2]) - f(center))^2) + ((f(points[2]) - f(center))^2))/(3.0)) < 0.001) 
            return  points[3],xs 
        end 
 
        x4 = points[2]+points[3]-points[1] 
        beta = 2.0 
        y_min =  f(points[3]) 
 
        if(f(x4) < y_min) 
            beta = 2.0 
            x5 = beta*x4 + (1-beta)*center 
 
            if(f(x5)< f(x4) && f(x5)< f(points[3]) && f(x5) < f(points[2])) 
                points[1] = x5 
            else 
                if(f(x5) > f(x4)) 
                    points[1] = x4 
                end 
            end 
        else 
            if f(points[3]) < f(x4) < f(points[2])
                points[1] = x4 
            else 
                if f(points[2]) < f(x4) < f(points[1]) 
                    points[1] = x4 
                end 
 
                points = sort(points, by=x -> f(x), rev=true) 
                beta = 0.5 
                x5 = beta * points[1] + (1 - beta) * center 
 
                if f(x5) < f(points[1]) 
                    points[1] = x5 
                else 
                    points[1] = points[3] + 0.5 * (points[1] - points[3]) 
                    points[2] = points[3] + 0.5 * (points[2] - points[3]) 
                end 
            end 
        end 
    end 
end